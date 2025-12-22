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

    /**
     * Escucha eventos de {@link DetailEvent} y se encarga de persistir los detalles que llegan
     * desde los integradores CLI3. La logica se restringe a validar el tipo de evento y delegar
     * el guardado al servicio {@link IDetailCli3Business}, dejando trazabilidad en logs en caso
     * de error o duplicados.
     */
    @Autowired
    IDetailCli3Business detailBusiness;

    @Override
    public void onApplicationEvent(DetailEvent event) {
        if (event.getTypeEvent().equals(DetailEvent.TypeEvent.SAVE_DETAIL) && event.getSource() instanceof Detail) {
            handleSaveDetail((Detail) event.getSource());
        }
    }

    /**
     * Intenta guardar el {@link Detail} recibido, logueando los casos en que el detalle ya existe
     * o cuando la capa de negocio informa errores. Se mantiene la traza del hash de los objetos
     * para facilitar depuracion cuando se emiten multiples eventos.
     */
    private void handleSaveDetail(Detail detail) {
        try {
            log.info("Listener: detail.id={}, hash={}, listenerHash={}",
            detail.getId(),
            System.identityHashCode(detail),
            System.identityHashCode(this));

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
