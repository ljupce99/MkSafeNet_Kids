package com.mksafenet.service;

import com.mksafenet.dto.ChatMessageDto;
import com.mksafenet.model.Scenario;
import com.mksafenet.repository.ScenarioRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;

    public ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    public record ScenarioOption(String key, String text) {}

//    public record Scenario(
//        int id,
//        String title,
//        List<ChatMessageDto> setupMessages,
//        String question,
//        List<ScenarioOption> options,
//        Set<String> correctAnswers,
//        String correctExplanation,
//        String wrongExplanation,
//        String consequenceType,
//        List<ChatMessageDto> consequenceMessages,
//        int points
//    ) {}

    public static final int TOTAL_SCENARIOS = 5;

    public List<ChatMessageDto> getIntroMessages(String studentName) {
        return List.of(
                msg("bot", "👋 Здраво " + studentName + "! Јас сум SafeBot — твој водич за интернет безбедност!", 0),
                msg("bot", "Денес ќе учиме за PHISHING 🎣 — кога лоши луѓе се обидуваат да те измамат онлајн.", 1200),
                msg("bot", "Тие се преправаат дека се некој на кој му веруваш (банка, игра или пријател) и сакаат да ти ги украдат лозинките или податоците 😈", 2600),
                msg("bot", "Но не се грижи — ќе научиш како да ги препознаеш и да се заштитиш 🦸‍♂️", 4200),
                msg("bot", "Ќе ти покажам неколку ситуации, а ти ќе одлучуваш што е правилно. Некои грешки ќе покажат што може да се случи.", 5600),
                msg("bot", "Ајде да почнеме со Сценарио 1 🚀", 7000)
        );
    }
    //TODO : Add Tabel of Contents for scenarios
    // and rendom get scenarios for each student
    public Scenario getScenario(int scenarioType) {
        List<Scenario> scenarioList = scenarioRepository.findAll();
        List<Scenario> scenarioListWithType = scenarioList.stream()
            .filter(s -> s.getTypeOfScenario() == scenarioType)
            .toList();

        Scenario scenarioReturned = scenarioListWithType.get(new Random().nextInt(scenarioListWithType.size()));

        return scenarioReturned;
    }

//    private Scenario scenario1() {
//        return new Scenario(
//            1,
//            "The Bank Email",
//            List.of(
//                msg("bot", "📧 Scenario 1: THE SUSPICIOUS EMAIL", 0),
//                msg("bot", "You just got this email...", 1200),
//                msg("system", "FROM: security@bank-urgent-alert.com\nSUBJECT: ⚠️ URGENT: Your account will be CLOSED!\n\nDear Customer,\n\nYour account has suspicious activity. You MUST click the link below in the next 24 hours or your account will be permanently deleted.\n\n👉 CLICK HERE TO VERIFY: http://bank-secure-login.xyz/verify\n\nBank Customer Service", 2400),
//                msg("bot", "Hmm... 🤔 What do you think about this email?", 4500)
//            ),
//            "What should you do with this email?",
//            List.of(
//                new ScenarioOption("A", "Click the link immediately! I don't want my account closed! 😰"),
//                new ScenarioOption("B", "Ignore it — it looks fake to me 🙅"),
//                new ScenarioOption("C", "Tell a parent or teacher and check with the real bank 🧑‍💼")
//            ),
//            Set.of("B", "C"),
//            "🎉 Great thinking! Real banks NEVER ask you to verify by clicking an email link. Look at that email address — 'bank-urgent-alert.com'? That's not a real bank! You should always check with a trusted adult first.",
//            "Oh no! You clicked the link — let's see what happened...",
//            "ACCOUNT_HACKED",
//            List.of(
//                msg("consequence", "🖥️ Loading: bank-secure-login.xyz...", 0),
//                msg("consequence", "📝 The fake website asks for your username and password...", 1500),
//                msg("consequence", "✅ 'Login successful' — but wait, something is WRONG!", 3000),
//                msg("consequence", "📱 ALERT: A new device logged into your account from Romania!", 4500),
//                msg("consequence", "💸 Transaction: -€2,500 sent to unknown account", 5800),
//                msg("consequence", "❌ Your account is now EMPTY. The hackers stole everything!", 7000),
//                msg("bot", "😔 This is what happens when you click phishing links. The fake website looked EXACTLY like the real bank!", 8500),
//                msg("bot", "💡 Remember: Real banks NEVER send urgent emails asking you to click links. Always go directly to the bank's website!", 10000)
//            ),
//            20
//        );
//    }
//
//    private Scenario scenario2() {
//        return new Scenario(
//            2,
//            "The Prize Pop-up",
//            List.of(
//                msg("bot", "🎮 Scenario 2: THE AMAZING PRIZE!", 0),
//                msg("bot", "You're playing your favorite online game when suddenly...", 1200),
//                msg("system", "🎊 CONGRATULATIONS! 🎊\n\nYOU ARE THE 1,000,000th VISITOR!\n\nYou've won FREE V-BUCKS / ROBUX / GEMS!\n\nTo claim your FREE prize, enter:\n• Your game username\n• Your game password\n• Your email\n\n⏰ This offer expires in 5 MINUTES!", 2400),
//                msg("bot", "Wow, a million gems for free! What do you do? 🤔", 4500)
//            ),
//            "A pop-up promises you free game rewards. What do you do?",
//            List.of(
//                new ScenarioOption("A", "Enter my username and password quickly before time runs out! 🏃"),
//                new ScenarioOption("B", "Close the pop-up — it's too good to be true! ❌"),
//                new ScenarioOption("C", "Ask a parent or teacher before doing anything 🙋")
//            ),
//            Set.of("B", "C"),
//            "✅ Smart move! If something sounds too good to be true online — it probably IS! Game companies NEVER give free prizes through random pop-ups. The countdown timer is a trick to make you panic and stop thinking clearly.",
//            "You entered your game login... let's see what happens next.",
//            "GAME_HACKED",
//            List.of(
//                msg("consequence", "📝 You type your username and password into the pop-up...", 0),
//                msg("consequence", "⏳ 'Processing your reward...'", 1800),
//                msg("consequence", "📧 You receive an email: 'Your account password was changed'", 3200),
//                msg("consequence", "😱 You try to log into your game — WRONG PASSWORD!", 4500),
//                msg("consequence", "💔 All your characters, skins, and progress are GONE. The hacker sold your account!", 5800),
//                msg("consequence", "😭 3 years of progress... lost in 2 minutes.", 7000),
//                msg("bot", "This is called a 'Credential Phishing' attack. They stole your password using a fake prize!", 8500),
//                msg("bot", "💡 Real games give prizes INSIDE the game, not through pop-ups asking for passwords!", 10000)
//            ),
//            20
//        );
//    }
//
//    private Scenario scenario3() {
//        return new Scenario(
//            3,
//            "Spot the Fake Link",
//            List.of(
//                msg("bot", "🔗 Scenario 3: THE DANGEROUS LINK", 0),
//                msg("bot", "Hackers are sneaky — they create FAKE websites that look EXACTLY like real ones!", 1200),
//                msg("bot", "The trick is in the web address (URL). Can you spot the dangerous one? 🕵️", 2500)
//            ),
//            "Which of these website links is FAKE and dangerous?",
//            List.of(
//                new ScenarioOption("A", "https://www.paypa1.com/login (notice the number 1 instead of letter l)"),
//                new ScenarioOption("B", "https://www.paypal.com/login"),
//                new ScenarioOption("C", "https://secure.paypal.com/checkout")
//            ),
//            Set.of("A"),
//            "🎯 Excellent detective work! 'paypa1.com' uses the NUMBER 1 instead of the LETTER l — easy to miss! Hackers call this 'typosquatting'. They buy domain names that look almost identical to real ones. Always look VERY carefully at web addresses!",
//            "You clicked on paypa1.com thinking it was PayPal...",
//            "DATA_STOLEN",
//            List.of(
//                msg("consequence", "🌐 Opening paypa1.com — it looks exactly like PayPal!", 0),
//                msg("consequence", "📝 You log in with your real PayPal email and password...", 1800),
//                msg("consequence", "💳 The site asks to 'verify' your credit card number...", 3200),
//                msg("consequence", "📸 The hacker now has your: Email ✓ Password ✓ Card number ✓ Card expiry ✓ CVV ✓", 4800),
//                msg("consequence", "🛍️ Unauthorized purchases start appearing on your parents' card!", 6200),
//                msg("consequence", "😤 Your parents get a bill for $847 they never spent!", 7500),
//                msg("bot", "One tiny character — '1' instead of 'l' — caused so much damage!", 9000),
//                msg("bot", "💡 ALWAYS check the web address carefully. Bookmark important sites so you always go to the right place!", 10500)
//            ),
//            20
//        );
//    }
//
//    private Scenario scenario4() {
//        return new Scenario(
//            4,
//            "The Urgent Password Request",
//            List.of(
//                msg("bot", "⚠️ Scenario 4: THE URGENT MESSAGE", 0),
//                msg("bot", "You get a message that seems very important and scary...", 1200),
//                msg("system", "🚨 IMPORTANT SECURITY NOTICE 🚨\n\nFrom: admin@school-security-team.com\nTo: You\n\nDear Student,\n\nWe detected a VIRUS on your school account. To protect your files, we need your password IMMEDIATELY.\n\nReply to this email with:\n- Your school username\n- Your password\n- Your date of birth\n\nIf we don't receive this in 1 HOUR, your account will be deleted along with all your homework!\n\n- IT Security Team", 2400),
//                msg("bot", "This looks really serious! Your homework might get deleted! What do you do? 🤔", 4800)
//            ),
//            "An IT admin is asking for your password urgently. What do you do?",
//            List.of(
//                new ScenarioOption("A", "Reply with my password immediately — I can't lose my homework! 😰"),
//                new ScenarioOption("B", "Ignore it completely 🙅"),
//                new ScenarioOption("C", "Talk to a real teacher or IT person at school in person to verify 👨‍🏫")
//            ),
//            Set.of("C"),
//            "🌟 Perfect answer! REAL IT administrators NEVER ask for your password — ever! They can fix problems without knowing your password. The scary 'your homework will be deleted' is just pressure to make you panic. Always verify with a real person IN PERSON!",
//            "You replied with your school password. Here's what happened...",
//            "IDENTITY_STOLEN",
//            List.of(
//                msg("consequence", "📧 You send your username, password, and date of birth...", 0),
//                msg("consequence", "🔑 The hacker logs into your school account!", 1800),
//                msg("consequence", "📝 They change all your submitted assignments — replacing them with wrong answers!", 3200),
//                msg("consequence", "📧 They send mean messages to your friends FROM YOUR ACCOUNT", 4800),
//                msg("consequence", "🔒 They change your password so YOU can't log back in!", 6000),
//                msg("consequence", "😰 You have to explain to your teacher why your account was hacked...", 7400),
//                msg("bot", "This is called a 'Social Engineering' attack — using FEAR and URGENCY to trick you!", 8800),
//                msg("bot", "💡 Golden rule: NO legitimate service or IT team will EVER ask for your password. Not by email, not by phone, not ever!", 10400)
//            ),
//            20
//        );
//    }
//
//    private Scenario scenario5() {
//        return new Scenario(
//            5,
//            "The Suspicious Attachment",
//            List.of(
//                msg("bot", "📎 Scenario 5: THE MYSTERY FILE", 0),
//                msg("bot", "This is the last challenge — and it's a tricky one!", 1200),
//                msg("bot", "You get an email from what looks like your friend's address...", 2400),
//                msg("system", "FROM: alex.smith@gmail.com\nSUBJECT: You have to try this game! 🎮\n\nHey!\n\nI found this AMAZING free game, you can get it here!\nJust download and run the file below:\n\n📎 FREE_MINECRAFT_PREMIUM.exe (45 MB)\n\nYou're gonna love it!! 😊\n\n- Alex", 3600),
//                msg("bot", "It's from your friend Alex! He loves games too. The file is attached. What do you do? 🤔", 5500)
//            ),
//            "Your friend sent you a game file to download. What do you do?",
//            List.of(
//                new ScenarioOption("A", "Download and open it — it's from my friend! 🎮"),
//                new ScenarioOption("B", "Delete it without opening — .exe files from emails are always dangerous! 🗑️"),
//                new ScenarioOption("C", "Message or call Alex to check if HE actually sent this before doing anything 📱")
//            ),
//            Set.of("B", "C"),
//            "🏆 Excellent! Hackers often HACK your friend's email account and then send viruses to everyone in their contacts — so the virus appears to come from a trusted person! Alex probably didn't send this. Never open .exe files from email. Always verify first!",
//            "You opened the file thinking it was from your friend...",
//            "VIRUS_INSTALLED",
//            List.of(
//                msg("consequence", "💾 Downloading FREE_MINECRAFT_PREMIUM.exe...", 0),
//                msg("consequence", "⚡ Running the file...", 1500),
//                msg("consequence", "👁️ The virus silently installs itself. You don't notice anything yet.", 2800),
//                msg("consequence", "🎥 It turns on your webcam and microphone without you knowing", 4000),
//                msg("consequence", "📁 It scans your computer for passwords, photos, and documents", 5200),
//                msg("consequence", "🌐 It connects to a hacker's server and uploads everything!", 6500),
//                msg("consequence", "📧 It then sends itself to everyone in YOUR contacts list too!", 7800),
//                msg("consequence", "💻 Finally, it encrypts all your files and demands €500 to unlock them!", 9000),
//                msg("bot", "Alex's account was hacked first. Then the virus used his email to trick YOU!", 10500),
//                msg("bot", "💡 NEVER open .exe files from email. Even from friends — always call or text them first to check!", 12000)
//            ),
//            20
//        );
//    }

    public List<ChatMessageDto> getTransitionMessages(int typeOfScenario, boolean correct, String studentName) {
        if (typeOfScenario < TOTAL_SCENARIOS) {
            if (correct) {
                String encouragement = switch (typeOfScenario % 3) {
                    case 0 -> "Гориш! 🔥";
                    case 1 -> "Импресивно! 🌟";
                    default -> "Само така продолжи! 💪";
                };
                return List.of(
                        msg("success", encouragement + " Преминуваме на следниот предизвик...", 0)
                );
            } else {
                return List.of(
                        msg("bot", "Сега знаеш на што да внимаваш. Ајде да продолжиме — следен предизвик!", 0)
                );
            }
        }
        return Collections.emptyList();
    }

    public List<String> calculateBadges(int score, List<com.mksafenet.model.ScenarioResponse> responses) {
        List<String> badges = new ArrayList<>();
        if (score == 100) badges.add("🏆 Perfect Defender");
        if (score >= 80) badges.add("🛡️ Phishing Hunter");
        if (score >= 60) badges.add("🔍 Smart Spotter");
        if (score >= 40) badges.add("📚 Cyber Learner");
        if (score < 40) badges.add("🌱 Rising Defender");

        boolean allEmailsCorrect = responses.stream()
            .filter(r -> r.getScenarioId() == 1 || r.getScenarioId() == 4)
            .allMatch(com.mksafenet.model.ScenarioResponse::isCorrect);
        if (allEmailsCorrect) badges.add("📧 Email Expert");

        return badges;
    }

    public String calculateGrade(int score) {
        if (score >= 95) return "A+";
        if (score >= 85) return "A";
        if (score >= 75) return "B+";
        if (score >= 65) return "B";
        if (score >= 55) return "C+";
        if (score >= 45) return "C";
        return "D";
    }

    private ChatMessageDto msg(String type, String text, int delayMs) {
        return ChatMessageDto.builder().type(type).text(text).delayMs(delayMs).build();
    }
}
