package edu.iua.nexus.integration.cli1.model.business.interfaces;

import edu.iua.nexus.integration.cli1.model.ProductCli1;
import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

import java.util.List;

public interface IProductCli1Business {

    public ProductCli1 load(String idCli1) throws NotFoundException, BusinessException;

    public List<ProductCli1> list() throws BusinessException;

    public Product loadExternal(ProductCli1 product) throws BusinessException, NotFoundException, FoundException;

}