package com.ssafy.flip.domain.connect.dto.response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public record MissionCancelDTO(
        Header header,
        Body body
) {
    public static MissionCancelDTO of(String amrId, String state) {
        return new MissionCancelDTO(
                new Header("MISSION_CANCEL", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))),
                new Body(state)
        );
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "header", Map.of(
                        "msgName", header.msgName(),
                        "time", header.time()
                ),
                "body", Map.of(
                        "state", body.state()
                )
        );
    }

    public record Header(
            String msgName,
            String time
    ) {}

    public record Body(
            String state
    ) {}
}
