package com.hscmt.common.props;

import lombok.Data;

import java.util.Map;

@Data
public class JpaProps {
    private Map<String, String> properties;
}
