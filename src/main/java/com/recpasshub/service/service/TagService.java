package com.recpasshub.service.service;

import com.recpasshub.service.dto.TagResponse;
import com.recpasshub.service.entity.Event;
import com.recpasshub.service.entity.Tag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    /**
     * Replaces the event's tags with one entry per distinct, non-blank name. The sport type
     * comes from the event. Does nothing when names is null.
     *
     * @param event the event to tag
     * @param names the tag names chosen by the organizer
     */
    public void replaceTags(Event event, List<String> names) {
        if (names == null) {
            return;
        }
        event.getTags().clear();
        Set<String> seen = new HashSet<>();
        for (String raw : names) {
            if (raw == null) {
                continue;
            }
            String name = raw.trim();
            if (name.isEmpty() || !seen.add(name.toLowerCase())) {
                continue;
            }
            event.getTags().add(Tag.builder().name(name).sportType(event.getSportType()).event(event).build());
        }
    }

    public List<TagResponse> toResponses(Event event) {
        List<Tag> tags = event.getTags() != null ? new ArrayList<>(event.getTags()) : new ArrayList<>();
        return tags
            .stream()
            .map(t -> new TagResponse(t.getTagGuid(), t.getName(), t.getSportType(), t.getCreatedAt()))
            .collect(Collectors.toList());
    }
}
