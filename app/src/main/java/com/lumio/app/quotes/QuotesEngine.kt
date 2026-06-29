package com.lumio.app.quotes

import java.util.Calendar

data class Quote(
    val text: String,
    val author: String,
    val category: QuoteCategory
)

enum class QuoteCategory(val emoji: String) {
    MOTIVATION("🔥"),
    PRODUCTIVITY("⚡"),
    HEALTH("💚"),
    WISDOM("🧠"),
    SUCCESS("🏆"),
    MINDFULNESS("🧘"),
    FRIENDSHIP("🤝"),
    COURAGE("💪")
}

object QuotesEngine {

    private val quotes = listOf(
        Quote("The secret of getting ahead is getting started.", "Mark Twain", QuoteCategory.MOTIVATION),
        Quote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar", QuoteCategory.MOTIVATION),
        Quote("The only way to do great work is to love what you do.", "Steve Jobs", QuoteCategory.SUCCESS),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius", QuoteCategory.MOTIVATION),
        Quote("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill", QuoteCategory.COURAGE),
        Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", QuoteCategory.MOTIVATION),
        Quote("Your time is limited, don't waste it living someone else's life.", "Steve Jobs", QuoteCategory.WISDOM),
        Quote("The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt", QuoteCategory.MOTIVATION),
        Quote("The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb", QuoteCategory.WISDOM),
        Quote("An investment in knowledge pays the best interest.", "Benjamin Franklin", QuoteCategory.WISDOM),
        Quote("Health is not about the weight you lose, but the life you gain.", "Unknown", QuoteCategory.HEALTH),
        Quote("Take care of your body. It's the only place you have to live.", "Jim Rohn", QuoteCategory.HEALTH),
        Quote("A healthy outside starts from the inside.", "Robert Urich", QuoteCategory.HEALTH),
        Quote("Your body hears everything your mind says.", "Naomi Judd", QuoteCategory.HEALTH),
        Quote("The groundwork for all happiness is good health.", "Leigh Hunt", QuoteCategory.HEALTH),
        Quote("Focus on being productive instead of busy.", "Tim Ferriss", QuoteCategory.PRODUCTIVITY),
        Quote("Either you run the day or the day runs you.", "Jim Rohn", QuoteCategory.PRODUCTIVITY),
        Quote("The key is not to prioritize what's on your schedule, but to schedule your priorities.", "Stephen Covey", QuoteCategory.PRODUCTIVITY),
        Quote("Done is better than perfect.", "Sheryl Sandberg", QuoteCategory.PRODUCTIVITY),
        Quote("Work smarter, not harder.", "Allen F. Morgenstern", QuoteCategory.PRODUCTIVITY),
        Quote("Almost everything will work again if you unplug it for a few minutes. Including you.", "Anne Lamott", QuoteCategory.MINDFULNESS),
        Quote("The present moment is the only moment available to us.", "Thich Nhat Hanh", QuoteCategory.MINDFULNESS),
        Quote("Peace comes from within. Do not seek it without.", "Buddha", QuoteCategory.MINDFULNESS),
        Quote("In the middle of difficulty lies opportunity.", "Albert Einstein", QuoteCategory.COURAGE),
        Quote("Life is 10% what happens to you and 90% how you react to it.", "Charles R. Swindoll", QuoteCategory.WISDOM),
        Quote("You are never too old to set another goal or to dream a new dream.", "C.S. Lewis", QuoteCategory.MOTIVATION),
        Quote("The harder I work, the luckier I get.", "Samuel Goldwyn", QuoteCategory.SUCCESS),
        Quote("Dream big and dare to fail.", "Norman Vaughan", QuoteCategory.COURAGE),
        Quote("Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau", QuoteCategory.SUCCESS),
        Quote("The only limit to our realization of tomorrow is our doubts of today.", "Franklin D. Roosevelt", QuoteCategory.MOTIVATION),
        Quote("What you get by achieving your goals is not as important as what you become.", "Zig Ziglar", QuoteCategory.WISDOM),
        Quote("Hardships often prepare ordinary people for an extraordinary destiny.", "C.S. Lewis", QuoteCategory.COURAGE),
        Quote("You miss 100% of the shots you don't take.", "Wayne Gretzky", QuoteCategory.COURAGE),
        Quote("Whether you think you can or think you can't, you're right.", "Henry Ford", QuoteCategory.MOTIVATION),
        Quote("The best revenge is massive success.", "Frank Sinatra", QuoteCategory.SUCCESS),
        Quote("I find that the harder I work, the more luck I seem to have.", "Thomas Jefferson", QuoteCategory.PRODUCTIVITY),
        Quote("Don't watch the clock; do what it does. Keep going.", "Sam Levenson", QuoteCategory.MOTIVATION),
        Quote("Quality is not an act, it is a habit.", "Aristotle", QuoteCategory.SUCCESS),
        Quote("We are what we repeatedly do. Excellence, then, is not an act but a habit.", "Aristotle", QuoteCategory.SUCCESS),
        Quote("Motivation is what gets you started. Habit is what keeps you going.", "Jim Ryun", QuoteCategory.PRODUCTIVITY),
        Quote("Small daily improvements are the key to staggering long-term results.", "Robin Sharma", QuoteCategory.PRODUCTIVITY),
        Quote("Your future is created by what you do today, not tomorrow.", "Robert Kiyosaki", QuoteCategory.MOTIVATION),
        Quote("The secret to living well and longer is: eat half, walk double, laugh triple and love without measure.", "Tibetan Proverb", QuoteCategory.HEALTH),
        Quote("He who has health has hope, and he who has hope has everything.", "Arabian Proverb", QuoteCategory.HEALTH),
        Quote("To keep the body in good health is a duty, otherwise we shall not be able to keep our mind strong and clear.", "Buddha", QuoteCategory.HEALTH),
        Quote("Sleeping is the best meditation.", "Dalai Lama", QuoteCategory.MINDFULNESS),
        Quote("The mind is everything. What you think you become.", "Buddha", QuoteCategory.MINDFULNESS),
        Quote("Happiness is not something ready made. It comes from your own actions.", "Dalai Lama", QuoteCategory.MINDFULNESS),
        Quote("You don't drown by falling in the water; you drown by staying there.", "Ed Cole", QuoteCategory.COURAGE),
        Quote("Start where you are. Use what you have. Do what you can.", "Arthur Ashe", QuoteCategory.MOTIVATION)
    )

    fun getTodayQuote(): Quote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }

    fun getRandomQuote(): Quote = quotes.random()

    fun getQuotesByCategory(category: QuoteCategory): List<Quote> =
        quotes.filter { it.category == category }

    fun getMorningQuote(): Quote =
        quotes.filter {
            it.category == QuoteCategory.MOTIVATION ||
            it.category == QuoteCategory.PRODUCTIVITY
        }.random()

    fun getHealthQuote(): Quote =
        quotes.filter { it.category == QuoteCategory.HEALTH }.random()

    fun getAllQuotes(): List<Quote> = quotes
}
