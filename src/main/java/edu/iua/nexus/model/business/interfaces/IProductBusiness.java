package edu.iua.nexus.model.business.interfaces;

import java.util.List;

import edu.iua.nexus.model.Product;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IProductBusiness {

    public List<Product> list() throws BusinessException;

    public Product load(long id) throws NotFoundException, BusinessException;

    public Product load(String product) throws NotFoundException, BusinessException;

    public Product add(Product product) throws FoundException, BusinessException;

    public Product update(Product product) throws NotFoundException, BusinessException, FoundException;

    public void delete(Product product) throws NotFoundException, BusinessException;

    public void delete(long id) throws NotFoundException, BusinessException;

}