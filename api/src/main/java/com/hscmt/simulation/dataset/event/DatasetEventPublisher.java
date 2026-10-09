package com.hscmt.simulation.dataset.event;

import com.hscmt.common.event.AbstractDomainEventPublisher;
import com.hscmt.simulation.dataset.domain.Dataset;
import com.hscmt.simulation.dataset.domain.MeasureDataset;
import com.hscmt.simulation.dataset.domain.PipeNetworkDataset;
import com.hscmt.simulation.dataset.dto.DatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.measure.MeasureDatasetUpsertDto;
import com.hscmt.simulation.dataset.dto.pn.PipeNetworkDatasetUpsertDto;
import com.hscmt.simulation.dataset.repository.DatasetRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DatasetEventPublisher extends AbstractDomainEventPublisher<Dataset> {
    public DatasetEventPublisher(DatasetRepository datasetRepository, ApplicationEventPublisher eventPublisher) {
        super(datasetRepository, eventPublisher);
    }

    /* 데이터셋 저장 후 이벤트 발행 */
    public Dataset saveAndPublish (Dataset entity) {
        return super.saveAndPublish(
                entity, saveEntity -> {
                    if (saveEntity instanceof MeasureDataset md) {
                        return new DatasetUpsertedEvent(
                                md.getDsId(),
                                md.getRltmYn(),
                                md.getTermTypeCd(),
                                md.getInqyTerm(),
                                md.getStrtDttm(),
                                md.getEndDttm(),
                                md.getRgstDttm()
                        );
                    } else {
                        return new DatasetUpdatedEvent(entity.getDsId());
                    }
                });
    }

    /* 업데이트 후 발행 */
    public void updateAndPublish (Dataset dataset, DatasetUpsertDto dto) {
        dataset.changeInfo(dto);

        super.publishAndClear(dataset, new DatasetUpdatedEvent(dataset.getDsId()));
        if (dataset instanceof MeasureDataset target) {
            super.publishAndClear(dataset, new DatasetUpsertedEvent(
                    target.getDsId(), target.getRltmYn(), target.getTermTypeCd(), target.getInqyTerm(), target.getStrtDttm(), target.getEndDttm(), target.getRgstDttm()
            ));
        }
    }

    /* 삭제 후 발행 */
    public void deleteAndPublish (Dataset entity) {
        super.deleteAndPublish(entity, new DatasetDeletedEvent(entity.getDsId()));
    }
}
