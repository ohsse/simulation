package com.hscmt.simulation.layer.event;

import com.hscmt.common.enumeration.YesOrNo;
import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.layer.domain.Layer;
import com.hscmt.simulation.layer.dto.LayerUpsertDto;
import com.hscmt.simulation.layer.repository.LayerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LayerEventPublisher extends AbstractDomainEventPublisher<Layer> {

    public LayerEventPublisher(LayerRepository repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }

    /* 저장 후 이벤트 발행 */
    public Layer saveAndPublish (Layer entity) {
        return super.saveAndPublish(
                entity,
                saveEntity -> new LayerUpsertedEvent(saveEntity.getLayerId(), saveEntity.getCrsyTypeCd(), saveEntity.getLayerStyles(), saveEntity.getMdfId()));
    }

//    public void saveAllAndPublish(List<Layer> layerList) {
//        repository.saveAll(layerList);
//        layerList.forEach(layer -> {
//            super.publishAndClear(layer, new LayerUpsertedEvent(layer.getLayerId(), layer.getCrsyTypeCd(), layer.getLayerStyles(), layer.getMdfId()));
//        });
//    }
//
//    public void publishAndClearLock(Layer layer) {
//        System.out.println("LayerEventPublisher.publishAndClear");
//        super.publishAndClear(layer, new LayerUpsertedEvent(layer.getLayerId(), layer.getCrsyTypeCd(), layer.getLayerStyles(), layer.getMdfId()));
//    }

    /* 수정 후 이벤트 발행 */
    public void updateAndPublish (Layer entity,LayerUpsertDto dto) {
        entity.changeInfo(dto);
        entity.changeUseAbleYn(YesOrNo.N);
        super.publishAndClear(entity, new LayerUpsertedEvent(entity.getLayerId(), entity.getCrsyTypeCd(), entity.getLayerStyles(), entity.getMdfId()));
    }

    /* 삭제 후 이벤트 발행 */
    public void deleteAndPublish (Layer layer) {
        super.deleteAndPublish(layer, new LayerDeletedEvent(layer.getLayerId()));
    }
}
