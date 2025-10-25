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
}
        ScoringResult result = scorer.score(cvText, job);

        System.out.println(result.toString());
    }
}
