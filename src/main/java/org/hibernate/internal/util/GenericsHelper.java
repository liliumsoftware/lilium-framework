/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.internal.util;

import org.hibernate.models.spi.MemberDetails;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Patched copy of Hibernate ORM 7.4.1's {@code org.hibernate.internal.util.GenericsHelper},
 * shipped in lilium-framework so it overrides the version inside {@code hibernate-core} on the
 * classpath.
 * <p>
 * Hibernate 7.4's {@code substituteTypeArguments}/{@code replaceTypeVariableWithArgument}
 * resolve type variables <em>by name</em> with no cycle detection. With self-bounded entity
 * generics that are ubiquitous in this framework — e.g.
 * {@code BaseEntity<E extends BaseEntity<E, ID>, ID>} and entity classes such as
 * {@code Product<E extends BaseEntity<E, Long>> extends BaseEntity<E, Long>} — a type variable
 * gets passed into a supertype parameter of the <em>same name</em>, so resolution recurses
 * forever and throws {@link StackOverflowError} while building the {@code EntityManagerFactory}.
 * <p>
 * The only change versus upstream is a {@code seen} set threaded through the recursion: when a
 * type variable is encountered while it is already being resolved on the current call stack, it
 * is erased to its upper bound instead of recursing again. Everything else is byte-for-byte the
 * upstream 7.4.1 source. Remove this class once the upstream bug (no cycle guard in
 * GenericsHelper) is fixed and the project moves to that Hibernate release.
 *
 * @author Gavin King
 */
public final class GenericsHelper {

	/**
	 * The type of the member inherited by the subclass from the supertype,
	 * as viewed from within the subclass.
	 * @param memberDetails The member, represented in the subclass
	 * @return The type of the member as it would be seen in the subclass
	 */
	public static Type actualMemberType(MemberDetails memberDetails) {
		return actualInheritedMemberType(
				memberDetails.getDeclaringType().toJavaClass(),
				memberDetails.toJavaMember()
		);
	}

	/**
	 * The type of the member inherited by the subclass from the supertype,
	 * as viewed from within the subclass.
	 * @param subclass The inheriting subclass
	 * @param superMember The member declared in the supertype
	 * @return The type of the member as it would be seen in the subclass
	 */
	public static Type actualInheritedMemberType(Class<?> subclass, Member superMember) {
		return substituteTypeVariables( getMemberType( superMember ),
				collectTypeArguments( subclass, superMember ) );
	}

	private static Map<TypeVariable<?>, Type> collectTypeArguments(
			Class<?> subclass, Member superMember) {
		final var superclass = superMember.getDeclaringClass();
		final var typeParameters = superclass.getTypeParameters();
		final Map<TypeVariable<?>, Type> typeMap =
				new HashMap<>( typeParameters.length );

		// There is no context to resolve the type variables if the subclass is the same as the superclass,
		// so just take the type parameter bounds instead
		if ( subclass == superclass ) {
			for ( var typeParameter : typeParameters ) {
				typeMap.put( typeParameter, typeParameter.getBounds()[0] );
			}
		}
		else {
			final var typeArguments = typeArguments( superclass, subclass );
			for ( int i = 0; i < typeParameters.length; i++ ) {
				typeMap.put( typeParameters[i], typeArguments[i] );
			}
		}
		return typeMap;
	}

	private static Type getMemberType(Member superMember) {
		if ( superMember instanceof Field field ) {
			return field.getGenericType();
		}
		else if ( superMember instanceof Method method ) {
			return method.getGenericReturnType();
		}
		else {
			throw new IllegalArgumentException( "Unsupported member: " + superMember );
		}
	}

	private static Type substituteTypeVariables(Type type, Map<TypeVariable<?>, Type> typeMap) {

		if ( type instanceof TypeVariable<?> typeVariable ) {
			final var substituted = typeMap.get( typeVariable );
			return substituted == null
					? Object.class
					: substituteTypeVariables( substituted, typeMap );
		}
		else if ( type instanceof ParameterizedType parameterizedType ) {
			final var args = parameterizedType.getActualTypeArguments();
			final var resolved = new Type[args.length];
			for ( int i = 0; i < args.length; i++ ) {
				resolved[i] = substituteTypeVariables( args[i], typeMap );
			}
			return new SimpleParameterizedType(
					(Class<?>) parameterizedType.getRawType(),
					resolved,
					parameterizedType.getOwnerType()
			);
		}
		else if ( type instanceof GenericArrayType genericArrayType ) {
			final var elementType =
					substituteTypeVariables( genericArrayType.getGenericComponentType(), typeMap );
			return new GenericArrayType() {
				@Override
				public Type getGenericComponentType() {
					return elementType;
				}

				@Override
				public String toString() {
					return elementType.getTypeName() + "[]";
				}
			};
		}
		else {
			return type;
		}
	}

	/**
	 * The erased type of the given type.
	 * @param type A type, possibly with type arguments
	 * @return The erased type
	 */
	public static Class<?> erasedType(Type type) {
		if ( type instanceof Class<?> clazz ) {
			return clazz;
		}
		else if ( type instanceof ParameterizedType parameterizedType ) {
			return erasedType( parameterizedType.getRawType() );
		}
		else if ( type instanceof TypeVariable<?> typeVariable ) {
			return erasedType( typeVariable.getBounds()[0] );
		}
		else if ( type instanceof GenericArrayType genericArrayType ) {
			return genericArrayType.getGenericComponentType() instanceof Class<?> elementClass
					? elementClass.arrayType()
					: Object[].class;
		}
		else {
			throw new IllegalArgumentException( "Cannot erase type: " + type );
		}
	}

	/**
	 * Get the type argument of the instantiation of the given generic
	 * type constructor which is a supertype of the given type expression.
	 * @param genericType A generic type constructor
	 * @param implementingType A type expression
	 * @return The type arguments assigned to parameters of the generic type constructor
	 */
	public static Type[] typeArguments(Class<?> genericType, Type implementingType) {
		if ( genericType.getTypeParameters().length == 0 ) {
			return EMPTY_TYPE_ARRAY;
		}
		else {
			final var instantiation =
					supertypeInstantiation( genericType, implementingType );
			if ( instantiation == null ) {
				throw new IllegalArgumentException(
						implementingType.getTypeName()
						+ " is not a subtype of "
						+ genericType.getName() );
			}
			return instantiation.getActualTypeArguments();
		}
	}

	private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

	/**
	 * A supertype of the given type which is an instantiation of the
	 * given generic type constructor.
	 * @param genericType A generic type constructor
	 * @param base A type which is assignable to some instantiation of
	 *             the given generic type constructor
	 * @return An instantiation of the generic type constructor which
	 *         is a supertype of the given type, or null if none exists
	 */
	public static ParameterizedType supertypeInstantiation(Class<?> genericType, Type base) {
		if ( base == null ) {
			return null;
		}

		final var clazz = erasedType( base );
		if ( clazz == null ) {
			return null;
		}

		if ( clazz == genericType ) {
			if ( base instanceof ParameterizedType result ) {
				return result;
			}
			// raw type used as-is (e.g. member declared directly on a generic class)
			// resolve type parameters to their upper bounds (erasure)
			final var typeParameters = clazz.getTypeParameters();
			if ( typeParameters.length > 0 ) {
				final var bounds = new Type[typeParameters.length];
				for ( int i = 0; i < typeParameters.length; i++ ) {
					bounds[i] = typeParameters[i].getBounds()[0];
				}
				return new SimpleParameterizedType( clazz, bounds, null );
			}
		}

		final var superclass = clazz.getGenericSuperclass();
		if ( superclass != null ) {
			final var type = substituteTypeArguments( superclass, base );
			if ( type instanceof ParameterizedType parameterizedType
					&& genericType.equals( parameterizedType.getRawType() ) ) {
				return parameterizedType;
			}

			final var parameterizedType =
					supertypeInstantiation( genericType, type );
			if ( parameterizedType != null ) {
				return parameterizedType;
			}
		}

		for ( var iface : clazz.getGenericInterfaces() ) {
			final var type = substituteTypeArguments( iface, base );
			if ( type instanceof ParameterizedType parameterizedType
					&& genericType.equals( parameterizedType.getRawType() ) ) {
				return parameterizedType;
			}

			final var parameterizedType =
					supertypeInstantiation( genericType, type );
			if ( parameterizedType != null ) {
				return parameterizedType;
			}
		}

		return null;
	}

	private static Type substituteTypeArguments(Type target, Type context) {
		return substituteTypeArguments( target, context, new HashSet<>() );
	}

	private static Type substituteTypeArguments(Type target, Type context, Set<TypeVariable<?>> seen) {
		if ( target instanceof ParameterizedType parameterizedType ) {
			return replaceTypeVariablesWithArguments( parameterizedType, context, seen );
		}
		else if ( target instanceof TypeVariable<?> typeVariable
					&& context instanceof ParameterizedType parameterizedContext ) {
			// Cycle guard (patch vs upstream 7.4.1): self-bounded generics such as
			// `Product<E extends BaseEntity<E, Long>> extends BaseEntity<E, Long>` pass a type
			// variable into a supertype parameter of the same name, which would otherwise recurse
			// forever. If we are already resolving this type variable, erase it to its bound.
			if ( !seen.add( typeVariable ) ) {
				return erasedType( typeVariable );
			}
			try {
				return replaceTypeVariableWithArgument( typeVariable, parameterizedContext, seen );
			}
			finally {
				seen.remove( typeVariable );
			}
		}
		else {
			return target;
		}
	}

	private static Type replaceTypeVariableWithArgument(
			TypeVariable<?> typeVariable, ParameterizedType context, Set<TypeVariable<?>> seen) {
		final var clazz = erasedType( context.getRawType() );
		if ( clazz == null ) {
			return null;
		}

		final var typeArguments = context.getActualTypeArguments();
		final var typeParameters = clazz.getTypeParameters();
		for ( int idx = 0; idx < typeParameters.length; idx++ ) {
			if ( typeVariable.getName().equals( typeParameters[idx].getName() ) ) {
				return substituteTypeArguments( typeArguments[idx], context, seen );
			}
		}

		return typeVariable;
	}

	private static ParameterizedType replaceTypeVariablesWithArguments(
			ParameterizedType parameterizedType, Type context, Set<TypeVariable<?>> seen) {
		final var typeArguments = parameterizedType.getActualTypeArguments();
		final var resolvedTypeArguments = new Type[typeArguments.length];
		for ( int idx = 0; idx < typeArguments.length; idx++ ) {
			resolvedTypeArguments[idx] = substituteTypeArguments( typeArguments[idx], context, seen );
		}
		return new SimpleParameterizedType(
				erasedType( parameterizedType ),
				resolvedTypeArguments,
				parameterizedType.getOwnerType()
		);
	}

	private record SimpleParameterizedType(Class<?> raw, Type[] args, Type owner)
			implements ParameterizedType {
		@Override
		public Type[] getActualTypeArguments() {
			return args;
		}

		@Override
		public Type getRawType() {
			return raw;
		}

		@Override
		public Type getOwnerType() {
			return owner;
		}

		@Override
		public String toString() {
			final var joiner = new StringJoiner( ", ", "<", ">" );
			for ( var type : args ) {
				joiner.add( type.getTypeName() );
			}
			return raw.getName() + joiner;
		}
	}
}
