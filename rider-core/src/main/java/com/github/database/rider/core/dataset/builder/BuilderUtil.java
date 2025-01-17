package com.github.database.rider.core.dataset.builder;

import com.github.database.rider.core.api.configuration.Orthography;
import com.github.database.rider.core.configuration.DBUnitConfig;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.metamodel.Attribute;

import java.math.BigDecimal;
import java.util.Date;

import static com.github.database.rider.core.util.ClassUtils.isOnClasspath;
import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.isEntityManagerActive;

public class BuilderUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuilderUtil.class.getName());
    private static final DBUnitConfig config = DBUnitConfig.fromGlobalConfig();

    public static String convertColumnCase(Column column, DBUnitConfig config) {
        return convertCase(column.getColumnName(), config);
    }

    public static String convertCase(String value, DBUnitConfig config) {
        if (value != null && config != null && !config.isCaseSensitiveTableNames()) {
            if (Orthography.UPPERCASE == config.getCaseInsensitiveStrategy()) {
                value = value.toUpperCase();
            } else {
                value = value.toLowerCase();
            }
        }
        return value;
    }

    public static String getColumnNameFromMetaModel(Attribute column) {
        String columnName = null;
        try {
            if (isEclipseLinkOnClasspath()) {
                columnName = ((AttributeImpl) column).getMapping().getField().getName();
            } else if (isHibernateOnClasspath() && isEntityManagerActive()) {
                AbstractEntityPersister entityMetadata = (AbstractEntityPersister) em().getEntityManagerFactory().unwrap(SessionFactory.class).getClassMetadata(column.getJavaMember().getDeclaringClass());
                columnName = entityMetadata.getPropertyColumnNames(column.getName())[0];
            }
        } catch (Exception e) {
            LOGGER.error("Could not extract database column name from column {} and type {}", column.getName(), column.getDeclaringType().getJavaType().getName(), e);
        }
        if (columnName == null) {
            columnName = convertCase(column.getName(), config);
        }
        return columnName;
    }

    public static boolean isHibernateOnClasspath() {
        return isOnClasspath("org.hibernate.Session");
    }

    public static boolean isEclipseLinkOnClasspath() {
        return isOnClasspath("org.eclipse.persistence.mappings.DirectToFieldMapping");
    }
}
