package edu.iua.nexus.integration.cli1.model.business.impl;

import edu.iua.nexus.integration.cli1.model.ProductCli1;
import edu.iua.nexus.integration.cli1.model.business.interfaces.IProductCli1Business;
import edu.iua.nexus.integration.cli1.model.repository.ProductCli1Repository;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import edu.iua.nexus.model.business.interfaces.IProductBusiness;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductCli1Business implements IProductCli1Business {

    @Autowired
    private ProductCli1Repository productDAO;

    @Autowired
    private IProductBusiness productBaseBusiness;

    // se puede llegar a implementar para que el sistema externo busque un producto por su idCli1
    @Override
    public ProductCli1 load(String idCli1) throws NotFoundException, BusinessException {
        Optional<ProductCli1> r;
        try {
            r = productDAO.findOneByIdCli1(idCli1);
        } catch (Exception e) {
            ProductCli1Business.log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
        if (r.isEmpty()) {
            throw NotFoundException.builder().message("No se encuentra el producto idCli1=" + idCli1).build();
        }
        return r.get();
    }

    // se puede llegar a implementar para que el sistema externo liste todos los productos
    @Override
    public List<ProductCli1> list() throws BusinessException {
        try {
            return productDAO.findAll();
        } catch (Exception e) {
            ProductCli1Business.log.error(e.getMessage(), e);
            throw BusinessException.builder().ex(e).build();
        }
    }

    @Override //este es LOAD External y no ADD External porque yo puedo meter un camion, conductor, cliente nuevo! pero no un prod nuevo!
    public Product loadExternal(ProductCli1 product) throws BusinessException, NotFoundException, FoundException {
        Optional<ProductCli1> foundProduct;

        //busco x idcli1, me fijo duplicados
        foundProduct = productDAO.findOneByIdCli1(product.getIdCli1());
        if (foundProduct.isPresent()) {
            return foundProduct.get();
        }
        foundProduct = productDAO.findProductCli1ByProduct(product.getProduct());
        if (foundProduct.isPresent()) {
            throw FoundException.builder()
                .message("Ya existe un producto con el nombre: " + product.getProduct())
                .build();
        }

        // si no lo tengo como prodCLi1, ME FIJO SI ESTA COMO PRODUCTO BASE!!
        //SI NO, ME DA ERROR, PORQUE EL SISTEMA EXTERNO NO PUEDE CREAR PRODUCTOS NUEVOS, TIENE Q USAR LOS Q 
        //ESTAN EN EL SISTEMA
        Product baseProduct = productBaseBusiness.load(product.getProduct());
        try {
            product.setId(baseProduct.getId());
            product.setProduct(baseProduct.getProduct());
            product.setDescription(baseProduct.getDescription());
            product.setThresholdTemperature(baseProduct.getThresholdTemperature());

            productDAO.insertProductCli1(baseProduct.getId(), product.getIdCli1());
            return baseProduct;

        } catch (DataIntegrityViolationException e) {
            Optional<ProductCli1> existingProduct = productDAO.findOneByIdCli1(product.getIdCli1());
            if (existingProduct.isPresent()) {
                return existingProduct.get();
            } else {
                throw BusinessException.builder().message("Error en el mapeo de producto").build();
            }
        }
    }


}