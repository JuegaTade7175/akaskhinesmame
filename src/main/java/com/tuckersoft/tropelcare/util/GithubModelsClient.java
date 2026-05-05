package com.tuckersoft.tropelcare.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class GithubModelsClient {

    private static final String SYSTEM_PROMPT = """
            Eres el sistema de clasificación de señales del TropelCare Signal Engine, desarrollado por Tuckersoft.
            Recibes señales emitidas por criaturas digitales llamadas Tropeles y debes clasificarlas.
            Responde ÚNICAMENTE con este JSON en una sola línea, sin texto adicional, sin markdown, sin bloques de código:
            {"signalType":"<TIPO>","severity":"<GRAVEDAD>","assignedUnit":"<UNIDAD>","recommendedAction":"<acción breve y concreta en español>"}
            
            Tipos válidos: HAMBRE, ABANDONO, MUTACION, FUGA, CONFLICTO, REPRODUCCION_MASIVA, SENAL_CORRUPTA
            Gravedades válidas: LEVE, MODERADO, GRAVE, CRITICO
            Unidades válidas: Laboratorio de Nutricion, Unidad de Bienestar, Division Genetica, Equipo de Contencion, Consejo de Mediacion, Control Demografico, Archivo de Senales
            """;

    private static final Set<String> VALID_TYPES = Set.of(
            "HAMBRE", "ABANDONO", "MUTACION", "FUGA", "CONFLICTO", "REPRODUCCION_MASIVA", "SENAL_CORRUPTA");
    private static final Set<String> VALID_SEVERITIES = Set.of("LEVE", "MODERADO", "GRAVE", "CRITICO");
    private static final Set<String> VALID_UNITS = Set.of(
            "Laboratorio de Nutricion", "Unidad de Bienestar", "Division Genetica",
            "Equipo de Contencion", "Consejo de Mediacion", "Control Demografico", "Archivo de Senales");

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String modelId;

    public GithubModelsClient(
            @Value("${github.token}") String token,
            @Value("${github.models.url}") String baseUrl,
            @Value("${github.models.model-id}") String modelId) {
        this.modelId = modelId;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Optional<ClassificationResult> classify(String rawContent) {
        try {
            Map<String, Object> body = Map.of(
                    "model", modelId,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", rawContent)
                    )
            );

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            return parseAndValidate(content);
        } catch (Exception e) {
            log.warn("[TROPEL-LOG] Fallo al clasificar con IA: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<ClassificationResult> parseAndValidate(String content) {
        try {
            // Extraer JSON aunque haya texto alrededor
            Pattern pattern = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(content);
            if (!matcher.find()) return Optional.empty();

            JsonNode node = objectMapper.readTree(matcher.group());
            String signalType = node.path("signalType").asText();
            String severity = node.path("severity").asText();
            String assignedUnit = node.path("assignedUnit").asText();
            String recommendedAction = node.path("recommendedAction").asText();
            String personalityNote = node.has("personalityNote") ? node.path("personalityNote").asText() : null;

            if (!VALID_TYPES.contains(signalType) || !VALID_SEVERITIES.contains(severity) || !VALID_UNITS.contains(assignedUnit)) {
                return Optional.empty();
            }

            return Optional.of(new ClassificationResult(signalType, severity, assignedUnit, recommendedAction, personalityNote));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record ClassificationResult(
            String signalType,
            String severity,
            String assignedUnit,
            String recommendedAction,
            String personalityNote
    ) {}
}
