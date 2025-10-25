import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ATS Pro Scoring (B3) - Prototype
 *
 * - Weighted scoring: skills, experience, education, formatting
 * - Formatting checks: presence of headers, detection of tables/images, flagged issues
 * - Missing skills suggestions + improvement recommendations
 *
 * Note: This is a prototype / rule-based implementation intended for hackathon demos.
 * For production we can swap in PDF parsers (Apache PDFBox), NLP (OpenNLP), or ML models.
 */
public class AtsProScorer {

    // Default weights (configurable)
    private double weightSkills = 0.40;
    private double weightExperience = 0.30;
    private double weightEducation = 0.15;
    private double weightFormatting = 0.15;

    // Formatting checks
    private static final String[] REQUIRED_HEADERS = {"experience", "work experience", "education", "skills", "contact", "summary"};
    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)\\s+years?");
    private static final Pattern DEGREE_PATTERN = Pattern.compile("\\b(bachelor|bsc|ba|master|msc|mba|phd|diploma)\\b", Pattern.CASE_INSENSITIVE);

    public AtsProScorer() { }

    public AtsProScorer(double wSkills, double wExperience, double wEducation, double wFormatting) {
        double total = wSkills + wExperience + wEducation + wFormatting;
        if (Math.abs(total - 1.0) > 1e-6)
            throw new IllegalArgumentException("Weights must sum to 1.0");
        this.weightSkills = wSkills;
        this.weightExperience = wExperience;
        this.weightEducation = wEducation;
        this.weightFormatting = wFormatting;
    }

     /**
     * Main scoring method.
     * @param cvText plain-text CV (extracted)
     * @param job JobDescription containing structured fields (title, requiredSkills, minExperienceYears, requiredEducationLevel)
     * @return ScoringResult with breakdown, missing skills, flagged issues, and recommendations.
     */
    public ScoringResult score(String cvText, JobDescription job) {
        // normalize text
        String cvLower = cvText == null ? "" : cvText.toLowerCase();

        // Extract skills from CV (simple approach: match against job.requiredSkills)
        Set<String> cvSkills = extractSkillsFromText(cvLower, job.getAllSkillCandidates());

        // Skills match
        List<String> requiredSkills = job.getRequiredSkills().stream().map(String::toLowerCase).collect(Collectors.toList());
        int matchedSkillsCount = 0;
        List<String> matchedSkills = new ArrayList<>();
        for (String s : requiredSkills) {
            if (cvSkills.contains(s)) {
                matchedSkillsCount++;
                matchedSkills.add(s);
            }
        }
        double skillsScore = requiredSkills.isEmpty() ? 1.0 : ((double) matchedSkillsCount) / requiredSkills.size();

        // Experience match
        int cvYears = estimateYearsOfExperience(cvLower);
        double experienceScore = computeExperienceScore(cvYears, job.getMinExperienceYears());

        // Education match
        String cvEducation = guessEducationLevel(cvLower);
        double educationScore = computeEducationScore(cvEducation, job.getRequiredEducationLevel());

        // Formatting score and flags
        FormattingAnalysis fmt = analyzeFormatting(cvText);
        double formattingScore = fmt.getFormattingScore();

        // Weighted total
        double totalScore = skillsScore * weightSkills
                + experienceScore * weightExperience
                + educationScore * weightEducation
                + formattingScore * weightFormatting;

        // Missing skills suggestions
        List<String> missingSkills = requiredSkills.stream()
                .filter(s -> !cvSkills.contains(s))
                .collect(Collectors.toList());

        // Recommendations - simple heuristic-based suggestions
        List<String> recommendations = generateRecommendations(matchedSkills, missingSkills, cvYears, job, fmt);

        return new ScoringResult(
                Math.round(totalScore * 100.0),
                Math.round(skillsScore * 100.0),
                Math.round(experienceScore * 100.0),
                Math.round(educationScore * 100.0),
                Math.round(formattingScore * 100.0),
                matchedSkills,
                missingSkills,
                fmt.getFlags(),
                recommendations
        );
    }
}
    

