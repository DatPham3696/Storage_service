package com.storage_service.storage.repository;

import com.storage_service.storage.dto.request.FileSearchRequest;
import com.storage_service.storage.entity.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileRepositoryImpl implements FileRepositoryCustom{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<File> searchFile(FileSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select e from File e " + createWhereQuery(request, values) + createOrderQuery(request.getSort());
        Query query = entityManager.createQuery(sql, File.class);
        values.forEach(query::setParameter);
        query.setFirstResult((request.getPage() - 1) * request.getSize());
        query.setMaxResults(request.getSize());
        return query.getResultList();
    }

    @Override
    public Long countFile(FileSearchRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(e) from File e" +createWhereQuery(request, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(FileSearchRequest request, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        boolean hasWhereClause = false;

        if (StringUtils.hasLength(request.getKeyword())) {
            if (!hasWhereClause) {
                sql.append(" where ");
                hasWhereClause = true;
            } else {
                sql.append(" and ");
            }
            sql.append(" (lower(e.fileName) like :keyword or lower(e.fileType) like :keyword)");
            values.put("keyword", "%" + request.getKeyword().toLowerCase() + "%");
        }

        if (StringUtils.hasLength(request.getFileName())) {
            if (!hasWhereClause) {
                sql.append(" where ");
                hasWhereClause = true;
            } else {
                sql.append(" and ");
            }
            sql.append(" e.fileName = :fileName ");
            values.put("fileName", request.getFileName());
        }

        if (StringUtils.hasLength(request.getFileType())) {
            if (!hasWhereClause) {
                sql.append(" where ");
                hasWhereClause = true;
            } else {
                sql.append(" and ");
            }
            sql.append(" e.fileType = :fileType ");
            values.put("fileType", request.getFileType());
        }

        if (request.getCreatedDate() != null) {
            if (!hasWhereClause) {
                sql.append(" where ");
                hasWhereClause = true;
            } else {
                sql.append(" and ");
            }
            sql.append(" e.createdDate = :createdDate ");
            values.put("createdDate", request.getCreatedDate());
        }

        return sql.toString();
    }
    private StringBuilder createOrderQuery(String sortBy) {
        StringBuilder hql = new StringBuilder(" ");
        if (StringUtils.hasLength(sortBy)) {
            hql.append(" order by e.").append(sortBy.replace(".", " "));
        } else {
            hql.append(" order by e.lastModifiedDate desc ");
        }
        return hql;
    }
}
