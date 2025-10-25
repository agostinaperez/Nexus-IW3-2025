package edu.iua.nexus.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import edu.iua.nexus.integration.cli3.model.business.interfaces.IDetailCli3Business;
import edu.iua.nexus.model.Detail;
import edu.iua.nexus.model.business.BusinessException;
import edu.iua.nexus.model.business.FoundException;
import edu.iua.nexus.model.business.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DetailEventListener implements ApplicationListener<DetailEvent> {

    @Override
    public void onApplicationEvent(DetailEvent event) {
        if (event.getTypeEvent().equals(DetailEvent.TypeEvent.SAVE_DETAIL) && event.getSource() instanceof Detail) {
            handleSaveDetail((Detail) event.getSource());
        }
    }

    @Autowired
    IDetailCli3Business detailBusiness;

    private void handleSaveDetail(Detail detail) {
        try {
            detailBusiness.add(detail);
        } catch (FoundException e) {
            log.error("El detalle con id={} ya existe", detail.getId(), e);
        } catch (BusinessException e) {
            log.error("Error al guardar el detalle con id={}", detail.getId(), e);
        } catch (NotFoundException e) {
            log.error("Error: ", e);
        }
    }
}