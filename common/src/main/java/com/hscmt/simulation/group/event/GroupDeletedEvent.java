package com.hscmt.simulation.group.event;


import com.hscmt.common.enumeration.GroupType;

public record GroupDeletedEvent(String grpId, GroupType groupType){
}
