package com.fastory.fastorybackend.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import com.fastory.fastorybackend.entity.Categoria;
import com.fastory.fastorybackend.entity.Producto;
import com.fastory.fastorybackend.entity.Repisa;
import com.fastory.fastorybackend.entity.Ubicacion;

public class ProductoSpecification {

    // 1. Filtro por ID de Categoría
    public static Specification<Producto> hasCategoria(Integer categoriaId) {
        return (root, query, criteriaBuilder) -> {
            if (categoriaId == null) {
                return criteriaBuilder.conjunction();
            }
            // Navega de Producto a Categoria
            Join<Producto, Categoria> categoriaJoin = root.join("categoria", JoinType.INNER);
            return criteriaBuilder.equal(categoriaJoin.get("idCategoria"), categoriaId);
        };
    }

    // 2. Filtro por Marca (usa el String directo de la entidad Producto)
    public static Specification<Producto> hasMarca(String marca) {
        return (root, query, criteriaBuilder) -> {
            if (marca == null || marca.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            // Usa LIKE para búsqueda parcial e IGNORA mayúsculas/minúsculas
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("marca")), "%" + marca.toLowerCase() + "%");
        };
    }

    // 3. Filtro por Stock Bajo Mínimo (Stock < StockMínimo)
    public static Specification<Producto> isStockBajoMinimo() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("stock"), root.get("stockMinimo"));
    }

    // --- NUEVAS ESPECIFICACIONES PARA Index.tsx ---

    /**
     * Filtra por nombre del producto (ignorando mayúsculas/minúsculas).
     */
    public static Specification<Producto> hasNombre(String nombre) {
        return (root, query, criteriaBuilder) -> {
            if (nombre == null || nombre.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // No aplicar filtro si está vacío
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("nombreProducto")),
                    "%" + nombre.toLowerCase() + "%");
        };
    }

    /**
     * Filtra por código de Repisa (JOIN con Ubicacion y Repisa).
     */
    public static Specification<Producto> hasRepisa(String repisaCodigo) {
        return (root, query, criteriaBuilder) -> {
            if (repisaCodigo == null || repisaCodigo.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Producto, Ubicacion> ubicacionJoin = root.join("ubicacion", JoinType.INNER);
            Join<Ubicacion, Repisa> repisaJoin = ubicacionJoin.join("repisa", JoinType.INNER);
            return criteriaBuilder.equal(criteriaBuilder.lower(repisaJoin.get("codigo")), repisaCodigo.toLowerCase());
        };
    }

    /**
     * Filtra por número de Fila (JOIN con Ubicacion).
     */
    public static Specification<Producto> hasFila(Integer fila) {
        return (root, query, criteriaBuilder) -> {
            if (fila == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Producto, Ubicacion> ubicacionJoin = root.join("ubicacion", JoinType.INNER);
            return criteriaBuilder.equal(ubicacionJoin.get("fila"), fila);
        };
    }

    /**
     * Filtra por número de Columna (JOIN con Ubicacion).
     */
    public static Specification<Producto> hasColumna(Integer columna) {
        return (root, query, criteriaBuilder) -> {
            if (columna == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Producto, Ubicacion> ubicacionJoin = root.join("ubicacion", JoinType.INNER);
            return criteriaBuilder.equal(ubicacionJoin.get("columna"), columna);
        };
    }
}