package com.csr.urlshortner.service;

import com.csr.urlshortner.dto.GlobalAnalyticsResponse.RecentClick;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void pushClick(RecentClick click) {
        String json = String.format(
            "{\"country\":\"%s\",\"origin\":\"%s\",\"clickedAt\":\"%s\"}",
            click.getCountry(), click.getOrigin(), click.getClickedAt()
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("click").data(json));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
