package ir.baho.framework.repository.impl;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.SqlAstNodeRenderingMode;
import org.hibernate.sql.ast.spi.StandardSqlAstTranslator;
import org.hibernate.sql.ast.tree.Statement;
import org.hibernate.sql.exec.spi.JdbcOperation;

class QueryTranslator extends StandardSqlAstTranslator<JdbcOperation> {

    public QueryTranslator(SessionFactoryImplementor sessionFactory, Statement statement) {
        super(sessionFactory, statement);
    }

    @Override
    protected SqlAstNodeRenderingMode getParameterRenderingMode() {
        return SqlAstNodeRenderingMode.INLINE_ALL_PARAMETERS;
    }

}
