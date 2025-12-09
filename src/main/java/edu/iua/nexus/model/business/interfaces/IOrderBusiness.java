package edu.iua.nexus.model.business.interfaces;

import edu.iua.nexus.model.Order;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.ConflictException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IOrderBusiness {

    public Page<Order> list(Pageable pageable, List<String> statuses) throws BusinessException;

    public Order load(long id) throws NotFoundException, BusinessException;

    public Order update(Order order) throws NotFoundException, BusinessException, FoundException;

    public byte[] getConciliationPdf(Long orderNumber) throws BusinessException, NotFoundException, ConflictException;

    public Map<String, Object> getConciliationJson(Long orderNumber) throws BusinessException, NotFoundException;
}
