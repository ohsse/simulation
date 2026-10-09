package com.hscmt.simulation.group.service;

import com.hscmt.simulation.common.config.jpa.SimulationTx;
import com.hscmt.simulation.group.domain.GroupBase;
import com.hscmt.simulation.group.dto.GroupDto;
import com.hscmt.simulation.group.dto.GroupItemDto;
import com.hscmt.simulation.group.dto.GroupItemUpsertDto;
import com.hscmt.simulation.group.dto.GroupUpsertDto;
import com.hscmt.simulation.group.event.GroupEventPublisher;
import com.hscmt.simulation.group.repository.GroupBaseRepository;

import java.util.*;
import java.util.stream.Collectors;

public abstract class GroupBaseService <E extends GroupBase> {

    private final GroupBaseRepository<E> repository;
    private final GroupEventPublisher<E> publisher;

    public GroupBaseService (GroupBaseRepository<E> repository, GroupEventPublisher<E> publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @SimulationTx
    public String upsertGroup (GroupUpsertDto groupUpsertDto) {
        E group = createEntity(groupUpsertDto);

        E saveGroup =  repository.save(group);

        List<GroupUpsertDto> children = groupUpsertDto.getChildren();
        if (children != null) {
            for (GroupUpsertDto child : children) {
                repository.findById(child.getGrpId())
                        .ifPresent(grp -> {
                            grp.changeParent(saveGroup);
                        });


                List<GroupItemUpsertDto> items = child.getItems();
                if (items != null) {
                    for (GroupItemUpsertDto item : items) {
                        item.setGrpId(child.getGrpId());
                    }
                    updateGroupItems(items);
                }
            }
        }
        return saveGroup.getGrpId();
    }

    public abstract void updateGroupItems (List<GroupItemUpsertDto> items);

    @SimulationTx
    public void upsertGroups(List<GroupUpsertDto> list) {
        for (GroupUpsertDto groupUpsertDto : list) {
            if (groupUpsertDto.getUpGrpId() != null) {
                if (groupUpsertDto.getUpGrpId().isEmpty() || groupUpsertDto.getUpGrpId().isBlank() || "ROOT".equals(groupUpsertDto.getUpGrpId())) {
                    groupUpsertDto.setUpGrpId(null);
                }
            }
        }

        // 1. 평탄화하면서 tempKey 기반 부모 관계 세팅
        List<GroupUpsertDto> flatList = flattenWithTempKeys(list);

        // tempKey → 실제 저장된 ID 매핑
        Map<String, String> tempKeyToGeneratedIdMap = new HashMap<>();

        // 2. 순서대로 저장
        for (GroupUpsertDto dto : flatList) {
            boolean isNew = (dto.getGrpId() == null || dto.getGrpId().isEmpty()) && !dto.getGrpId().equals("ROOT");

            // 부모 ID가 tempKey면 실제 ID로 변환
            if (dto.getUpGrpId() != null) {
                dto.setUpGrpId(tempKeyToGeneratedIdMap.getOrDefault(dto.getUpGrpId(), dto.getUpGrpId()));
            }

            E entity = isNew ? createEntity(dto) : repository.findById(dto.getGrpId()).orElse(null);
            if (entity != null) {
                entity.changeInfo(dto);
            }

            // 부모 관계까지 바로 설정
            if (dto.getUpGrpId() != null) {
                repository.findById(dto.getUpGrpId()).ifPresent(entity::changeParent);
            }

            E saved = entity != null ? repository.save(entity) : null;

            if (isNew) {
                tempKeyToGeneratedIdMap.put(generateTempKey(dto), saved.getGrpId());
            }

            List<GroupItemUpsertDto> items = dto.getItems();
            if (items != null) {
                for (GroupItemUpsertDto item : items) {
                    item.setGrpId(saved != null ? saved.getGrpId() : null);
                }
                updateGroupItems(items);
            }

        }
    }

    /**
     * tempKey를 부여하면서 평탄화
     */
    private List<GroupUpsertDto> flattenWithTempKeys(List<GroupUpsertDto> list) {
        List<GroupUpsertDto> result = new ArrayList<>();
        for (GroupUpsertDto dto : list) {
            String tempKey = generateTempKey(dto);
            flattenRecursiveWithTempKey(dto, null, tempKey, result);
        }
        return result;
    }

    private void flattenRecursiveWithTempKey(GroupUpsertDto dto, String parentTempKey, String tempKey, List<GroupUpsertDto> result) {
        // 부모가 있으면 upGrpId에 tempKey 할당
        if (parentTempKey != null && (dto.getUpGrpId() == null || dto.getUpGrpId().isBlank())) {
            dto.setUpGrpId(parentTempKey);
        }

        result.add(dto);

        if (dto.hasChildren()) {
            for (GroupUpsertDto child : dto.getChildren()) {
                String childTempKey = generateTempKey(child);
                flattenRecursiveWithTempKey(child, tempKey, childTempKey, result);
            }
        }
    }

    public abstract List<E> findAllGroupRecursive (List<String> ids) ;

    @SimulationTx
    public void deleteGroups (List<String> ids) {
        List<E> deleteEntities = findAllGroupRecursive(ids);

        for (E entity : deleteEntities) {
            publisher.deleteAndPublish(entity);
        }

        repository.deleteAll(deleteEntities);
    }

    /* 그룹목록 및 하위 아이템 목록 포함 */
    public abstract GroupDto getGroupsWithChildrenAndItems(String grpId);
    /* 엔터티 생성 */
    public abstract E createEntity (GroupUpsertDto dto);
    /* 그룹아이디 초기화 */
    public abstract void updateGroupIdToNull (String grpId);
    /* 그룹아이디 초기화 */
    public abstract void updateGroupIdToNull (List<String> grpIds);

    /* 구조체 없는 리스트로 변경 */
    public List<GroupUpsertDto> flatten(List<GroupUpsertDto> list) {
        List<GroupUpsertDto> result = new ArrayList<>();
        for (GroupUpsertDto dto : list) {
            result.add(dto);
            if (dto.hasChildren()) {
                result.addAll(flatten(dto.getChildren()));
            }
        }
        return result;
    }

    /* 재귀구조 */
    public List<GroupDto> hierarchy (List<GroupDto> flatList) {
        Map<String, GroupDto> map = new HashMap<>();
        List<GroupDto> roots = new ArrayList<>();

        for (GroupDto dto : flatList) {
            map.put(dto.getGrpId(), dto);
            dto.setChildren(new ArrayList<>());
        }

        for (GroupDto dto : flatList) {
            String upGrpId = dto.getUpGrpId();
            if (upGrpId == null) {
                roots.add(dto);
            } else {
                GroupDto parent = map.get(upGrpId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return roots;
    }

    /* 재귀구조 + 하위 항목 */
    public GroupDto hierarchyWithItems (List<GroupDto> flatList, List<GroupItemDto> itemList) {

        for (GroupDto dto : flatList) {
            dto.setItems (Optional.ofNullable(itemList)
                    .orElse(new ArrayList<>())
                    .stream()
                    .filter(item -> Objects.equals(item.getGrpId(), dto.getGrpId()))
                    .collect(Collectors.toList()));
        }

        Set<GroupItemDto> itemSet = itemList.stream().collect(Collectors.toSet());

        for (GroupDto flatItem : flatList) {
            List<GroupItemDto> items = flatItem.getItems();
            for (GroupItemDto item : items) {
                GroupItemDto removeItem = null;
                if (itemSet.contains(item)) {
                    removeItem = item;
                }

                if (removeItem != null) {
                    itemSet.remove(removeItem);
                }
            }
        }

        List <GroupDto> rootList = hierarchy(flatList);
        sortHierarchyList(rootList);

        GroupDto rootGroup = new GroupDto();
        rootGroup.setGrpNm("ROOT");
        rootGroup.setGrpId("ROOT");
        rootGroup.setChildren(rootList);

        if (!itemSet.isEmpty()) {
            rootGroup.setItems(new ArrayList<>(itemSet));
        }

        return rootGroup;
    }

    /* 재귀정렬 */
    public void sortHierarchyList (List<GroupDto> groupDtoList) {
        for (GroupDto dto : groupDtoList) {
            dto.getItems().sort(
                    Comparator.comparing(GroupItemDto::getSortOrd, Comparator.nullsLast(Integer::compareTo))
            );

            dto.getChildren().sort(
                    Comparator.comparing(GroupDto::getSortOrd, Comparator.nullsLast(Integer::compareTo))
            );

            sortHierarchyList(dto.getChildren());
        }
    }

    /* 템프키 생성 */
    private String generateTempKey(GroupUpsertDto dto) {
        return String.valueOf(System.identityHashCode(dto));
    }
}
