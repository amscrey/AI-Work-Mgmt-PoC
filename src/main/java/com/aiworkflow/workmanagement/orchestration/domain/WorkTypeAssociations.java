package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapping of work-type keywords to role names.
 */
public class WorkTypeAssociations {

    private final Map<String, String> keywordToRole;

    public WorkTypeAssociations(Map<String, String> keywordToRole) {
        this.keywordToRole = keywordToRole == null ? Map.of() : new LinkedHashMap<>(keywordToRole);
    }

    public Map<String, String> getKeywordToRole() {
        return Collections.unmodifiableMap(keywordToRole);
    }

    public static WorkTypeAssociations fromRoleKeywords(Map<String, List<String>> roleKeywords) {
        Map<String, String> map = new LinkedHashMap<>();
        List<String> sortedRoles = new ArrayList<>(roleKeywords.keySet());
        Collections.sort(sortedRoles);
        for (String role : sortedRoles) {
            List<String> keywords = roleKeywords.getOrDefault(role, List.of());
            for (String keyword : keywords) {
                map.putIfAbsent(keyword, role);
            }
        }
        return new WorkTypeAssociations(map);
    }
}
