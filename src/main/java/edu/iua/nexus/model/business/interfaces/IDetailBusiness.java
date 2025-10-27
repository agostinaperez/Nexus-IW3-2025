package edu.iua.nexus.model.business.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;

public interface IDetailBusiness {
    public Detail load(long id) throws NotFoundException, BusinessException;

    public List<Detail> listByOrder(long idOrder) throws NotFoundException, BusinessException;

    Detail add(Detail detail) throws FoundException, BusinessException;

    Page<Detail> listByOrder(Order order, Pageable pageable);
}
