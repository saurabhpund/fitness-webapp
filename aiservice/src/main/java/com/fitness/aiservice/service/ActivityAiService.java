package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {
    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPrompt(activity);
        String aiResponse = geminiService.getAnswer(prompt);
//        log.info("Response from Ai: " + aiResponse);
        return processAiResponse(activity, aiResponse);
    }

    public Recommendation processAiResponse(Activity activity, String aiResponse){
       try{
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(aiResponse);
        JsonNode textNode = rootNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text");

        String jsonContent = textNode.asText()
                .replaceAll("```json\\n", "")
                .replaceAll("\\n```", "").trim();

        log.info("Parsed response : {}", jsonContent);

        // Crafting analysis fields from jsonContent
        StringBuilder fullAnalysis = new StringBuilder();
        JsonNode analysisJson = mapper.readTree(jsonContent);
        JsonNode analysisNode = analysisJson.path("analysis");
        parseAnalysis(fullAnalysis, analysisNode, "overall", "Overall:");
        parseAnalysis(fullAnalysis, analysisNode, "pace", "Pace:");
        parseAnalysis(fullAnalysis, analysisNode, "heartrate", "Heart Rate:");
        parseAnalysis(fullAnalysis, analysisNode, "caloriesBurned", "Calories Burned:");

        // Extracting Improvement Fields from jsonContent or ai response
           List<String> improvements = extractImprovements(analysisJson.path("improvements"));

           // Extracting Suggestions Fields from jsonContent or ai response
           List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));

           // Extracting Suggestions Fields from jsonContent or ai response
           List<String> safetyGuidelines = extractSafetyGuidelines(analysisJson.path("safety"));

           return Recommendation.builder()
                   .activityId(activity.getId())
                   .userId(activity.getUserId())
                   .activityType(activity.getType())
                   .recommendation(fullAnalysis.toString().trim())
                   .improvement(improvements)
                   .suggestion(suggestions)
                   .safety(safetyGuidelines).build();

       }catch (Exception e){
           log.error(e.getMessage());
           return createDefaultRecommendation(activity);
       }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvement(Collections.singletonList("Continue with the current routine"))
                .suggestion(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList("Always warmup before exercise", "Stay hydrated", "Listen to your body"))
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safetyGuidelines = new ArrayList<>();
        if(safetyNode.isArray()){
            safetyNode.forEach(safety ->{
                safetyGuidelines.add(safety.asText());
            });
        }
        return safetyGuidelines.isEmpty() ? Collections.singletonList("Follow general safety guidelines") : safetyGuidelines;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if(suggestionsNode.isArray()){
            suggestionsNode.forEach(suggestion -> {
                String area = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s : %s", area, description));
            });
        }
        return suggestions.isEmpty() ? Collections.singletonList("No Suggestion provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementNode) {
        List<String> improvements = new ArrayList<>();
        if(improvementNode.isArray()){
            improvementNode.forEach(improvement -> {
                String area = improvement.path("areas").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s : %s", area, detail));
            });
        }

        return improvements.isEmpty() ? Collections.singletonList("No specific improvement provided") : improvements;

    }

    private void parseAnalysis(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if(!analysisNode.isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPrompt(Activity activity) {
        return String.format("""
                Analyze this fitness activity and provide detailed recommendations in the following 
                {
                  "analysis": {
                    "overall": "Overall analysis here",
                    "pace": "Pace analysis here",
                    "heartrate": "Heartrate analysis here",
                    "caloriesBurned": "Calories analysis here"
                  },
                  "improvements": [
                    {
                      "areas": "Area name",
                      "recommendation": "Detailed recommendation"
                    }
                  ],
                  "suggestions": [
                    {
                      "workout": "Workout name",
                      "description": "Detailed workout description"
                    }
                  ],
                  "safety": [
                    "Safety point 1",
                    "Safety point 2"
                  ]
                }
                
                Analyze this activity:
                Activity Type : %s
                Duration: %d minutes
                Calories Burned: %d
                Additional Metrics: %s
                
                Provide detailed analysis focusing on performance, improvements, next workout suggestions and safety guidelines.
                Ensure the response follows the EXACT JSON format shown above
                """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
                );
    }
}
