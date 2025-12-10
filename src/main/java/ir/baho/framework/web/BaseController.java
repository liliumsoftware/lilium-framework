package ir.baho.framework.web;

import ir.baho.framework.converter.EnumConverter;
import ir.baho.framework.dto.BaseDtoSimple;
import ir.baho.framework.dto.BaseFileDto;
import ir.baho.framework.dto.EntityMetadata;
import ir.baho.framework.dto.IdValue;
import ir.baho.framework.dto.RevisionDto;
import ir.baho.framework.dto.Tree;
import ir.baho.framework.exception.NotModifiedException;
import ir.baho.framework.exception.PreconditionFailedException;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.metadata.SummaryCollection;
import ir.baho.framework.metadata.SummaryPage;
import ir.baho.framework.service.CurrentUser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public abstract class BaseController<C extends BaseController<C>> {

    protected final Map<String, SseEmitter> sses = new ConcurrentHashMap<>();

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    @SuppressWarnings("rawtypes")
    protected PagedResourcesAssembler pagedResourcesAssembler;

    @Autowired
    protected MessageResource messageResource;

    @SuppressWarnings("unchecked")
    protected <U extends CurrentUser> U currentUser() {
        return (U) applicationContext.getBean(CurrentUser.class);
    }

    @SuppressWarnings("unchecked")
    protected C self() {
        return (C) WebMvcLinkBuilder.methodOn(getClass());
    }

    protected Link linkAll(Object invocationValue) {
        return WebMvcLinkBuilder.linkTo(invocationValue).withRel("all");
    }

    protected Link linkSelf(Object invocationValue) {
        return WebMvcLinkBuilder.linkTo(invocationValue).withSelfRel();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <D extends BaseDtoSimple<?, ID>, ID extends Serializable & Comparable<ID>> EntityModel<EntityMetadata<ID>> toMetadataModel(D entity, Link... links) {
        return EntityModel.of(new EntityMetadata(entity.getId(), entity.getLastModifiedDate(), entity.getVersion()), links);
    }

    protected <D> EntityModel<D> toModel(D entity, Link... links) {
        return EntityModel.of(entity, links);
    }

    @SuppressWarnings("unchecked")
    protected <D> PagedModel<EntityModel<D>> toModel(Page<D> page, Function<D, Link> link) {
        PagedModel<EntityModel<D>> pagedModel = pagedResourcesAssembler.toModel(page, new SimpleRepresentationModelAssembler<D>() {
            @Override
            public void addLinks(EntityModel<D> resource) {
                resource.add(link.apply(resource.getContent()));
            }

            @Override
            public void addLinks(CollectionModel<EntityModel<D>> resources) {
            }
        });
        if (page instanceof SummaryPage<D, ?> summaryPage) {
            return new SummaryPagedModel<EntityModel<D>, Object>(pagedModel, summaryPage.getSummary());
        }
        return pagedModel;
    }

    @SuppressWarnings("unchecked")
    protected <D> PagedModel<EntityModel<D>> toModel(Page<D> page) {
        PagedModel<EntityModel<D>> pagedModel = pagedResourcesAssembler.toModel(page);
        if (page instanceof SummaryPage<D, ?> summaryPage) {
            return new SummaryPagedModel<EntityModel<D>, Object>(pagedModel, summaryPage.getSummary());
        }
        return pagedModel;
    }

    protected <D> CollectionModel<EntityModel<D>> toModel(Collection<D> collection, Link link, Function<D, Link> links) {
        CollectionModel<EntityModel<D>> collectionModel = CollectionModel.of(collection.stream().map(d -> EntityModel.of(d).add(links.apply(d))).toList()).add(link);
        if (collection instanceof SummaryCollection<D, ?> summaryCollection) {
            return new SummaryCollectionModel<>(collectionModel, summaryCollection.getSummary());
        }
        return collectionModel;
    }

    protected <D> CollectionModel<EntityModel<D>> toModel(Collection<D> collection, Link link) {
        CollectionModel<EntityModel<D>> collectionModel = CollectionModel.of(collection.stream().map(EntityModel::of).toList()).add(link);
        if (collection instanceof SummaryCollection<D, ?> summaryCollection) {
            return new SummaryCollectionModel<>(collectionModel, summaryCollection.getSummary());
        }
        return collectionModel;

    }

    @SuppressWarnings("unchecked")
    protected <D, N extends Number & Comparable<N>> PagedModel<EntityModel<RevisionDto<N, D>>> toRevisionModel(Page<RevisionDto<N, D>> page, Function<RevisionDto<N, D>, Link> links) {
        return pagedResourcesAssembler.toModel(page, new SimpleRepresentationModelAssembler<RevisionDto<N, D>>() {
            @Override
            public void addLinks(EntityModel<RevisionDto<N, D>> resource) {
                resource.add(links.apply(resource.getContent()));
            }

            @Override
            public void addLinks(CollectionModel<EntityModel<RevisionDto<N, D>>> resources) {
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected <D, N extends Number & Comparable<N>> PagedModel<EntityModel<RevisionDto<N, D>>> toRevisionModel(Page<RevisionDto<N, D>> page) {
        return pagedResourcesAssembler.toModel(page);
    }

    protected <D, N extends Number & Comparable<N>> CollectionModel<EntityModel<RevisionDto<N, D>>> toRevisionModel(Collection<RevisionDto<N, D>> collection, Link link, Function<RevisionDto<N, D>, Link> links) {
        List<EntityModel<RevisionDto<N, D>>> revisions = collection.stream().map(e -> EntityModel.of(e).add(links.apply(e))).toList();
        return CollectionModel.of(revisions).add(link);
    }

    protected <D, N extends Number & Comparable<N>> CollectionModel<EntityModel<RevisionDto<N, D>>> toRevisionModel(Collection<RevisionDto<N, D>> collection, Link link) {
        List<EntityModel<RevisionDto<N, D>>> revisions = collection.stream().map(EntityModel::of).toList();
        return CollectionModel.of(revisions).add(link);
    }

    protected <E extends Enum<E>> CollectionModel<EntityModel<IdValue<String, String>>> toModel(E[] values, Link... links) {
        String prefix = EnumConverter.getPrefix(values[0]);
        return CollectionModel.of(Stream.of(values)
                .map(e -> new IdValue<>(e.name(), messageResource.getMessageOrDefault(prefix + "." + e.name(), e.name())))
                .map(EntityModel::of).toList()).add(links);
    }

    protected URI toUri(Object invocationValue) {
        return WebMvcLinkBuilder.linkTo(invocationValue).toUri();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <D extends Tree<D, ?>> List<D> toTree(List<D> nodes, Function<D, Comparable> sort) {
        List<D> roots = new ArrayList<>();
        for (D node : nodes) {
            if (node.getParentId() != null) {
                nodes.stream().filter(n -> n.getId().equals(node.getParentId())).findAny().ifPresent(p -> {
                    p.getChildren().add(node);
                    p.getChildren().sort(Comparator.comparing(sort));
                });
            } else {
                roots.add(node);
            }
        }
        roots.sort(Comparator.comparing(sort));
        return roots;
    }

    protected ResponseEntity<Resource> show(BaseFileDto<?, ?> file) {
        String name = UriUtils.encode(file.getName(), StandardCharsets.UTF_8);
        ContentDisposition contentDisposition = ContentDisposition.inline().filename(name).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(file.getType()));
        headers.setContentLength(file.getSize());
        return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(file.getValue()));
    }

    protected ResponseEntity<Resource> download(BaseFileDto<?, ?> file) {
        String name = UriUtils.encode(file.getName(), StandardCharsets.UTF_8);
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(name).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(file.getType()));
        headers.setContentLength(file.getSize());
        return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(file.getValue()));
    }

    protected ResponseEntity<StreamingResponseBody> show(String name, String type, StreamingResponseBody stream) {
        name = UriUtils.encode(name, StandardCharsets.UTF_8);
        ContentDisposition contentDisposition = ContentDisposition.inline().filename(name).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(type));
        return ResponseEntity.ok().headers(headers).body(stream);
    }

    protected ResponseEntity<StreamingResponseBody> download(String name, String type, StreamingResponseBody stream) {
        name = UriUtils.encode(name, StandardCharsets.UTF_8);
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(name).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType(type));
        return ResponseEntity.ok().headers(headers).body(stream);
    }

    protected void checkFileSize(int maxSize, MultipartFile... multipart) {
        for (MultipartFile file : multipart) {
            if (file.getSize() > maxSize) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large: " + file.getOriginalFilename());
            }
        }
    }

    protected byte[] partToBytes(MultipartFile multipart) {
        try {
            return multipart.getBytes();
        } catch (IOException e) {
            throw new MultipartException("Could not parse multipart file", e);
        }
    }

    protected String partToName(MultipartFile multipart) {
        String name = multipart.getOriginalFilename() != null ? multipart.getOriginalFilename() : multipart.getName();
        return URLDecoder.decode(name, StandardCharsets.UTF_8);
    }

    protected void subscribe(SseEmitter sseEmitter) {
        sses.put(currentUser().username(), sseEmitter);
        sseEmitter.onCompletion(() -> sses.values().remove(sseEmitter));
        sseEmitter.onError(t -> sses.values().remove(sseEmitter));
        sseEmitter.onTimeout(sseEmitter::complete);
    }

    protected void subscribe(String key, SseEmitter sseEmitter) {
        sses.put(key, sseEmitter);
        sseEmitter.onCompletion(() -> sses.values().remove(sseEmitter));
        sseEmitter.onError(t -> sses.values().remove(sseEmitter));
        sseEmitter.onTimeout(sseEmitter::complete);
    }

    protected SseEmitter send(Object value) {
        SseEmitter sseEmitter = sses.get(currentUser().username());
        if (sseEmitter != null) {
            try {
                sseEmitter.send(value);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return sseEmitter;
    }

    protected SseEmitter send(String key, Object value) {
        SseEmitter sseEmitter = sses.get(key);
        if (sseEmitter != null) {
            try {
                sseEmitter.send(value);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return sseEmitter;
    }

    @SneakyThrows
    protected List<SseEmitter> sendAll(Object value) {
        List<SseEmitter> sseEmitters = new ArrayList<>();
        for (SseEmitter s : sses.values()) {
            s.send(value);
            sseEmitters.add(s);
        }
        return sseEmitters;
    }

    protected void checkPrecondition(Supplier<EntityMetadata<?>> metadataSupplier) {
        WebRequest webRequest = getWebRequest();
        if (webRequest == null) {
            return;
        }
        String header = webRequest.getHeader(HttpHeaders.IF_MATCH);
        if (header == null) {
            return;
        }
        EntityMetadata<?> metadata = metadataSupplier.get();
        if (!header.equals(String.valueOf(metadata.version()))) {
            throw new PreconditionFailedException(metadata.version());
        }
    }

    protected void checkNotModified(Supplier<EntityMetadata<?>> metadataSupplier) {
        WebRequest webRequest = getWebRequest();
        if (webRequest == null) {
            return;
        }
        if (webRequest.getHeader(HttpHeaders.IF_NONE_MATCH) == null && webRequest.getHeader(HttpHeaders.IF_MODIFIED_SINCE) == null) {
            return;
        }
        EntityMetadata<?> metadata = metadataSupplier.get();
        if (webRequest.checkNotModified(String.valueOf(metadata.version()), metadata.lastModifiedDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())) {
            throw new NotModifiedException();
        }
    }

    private WebRequest getWebRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return new ServletWebRequest(attrs.getRequest(), attrs.getResponse());
    }

}
