package com.example.trackpoints.utils

import com.example.trackpoints.data.model.Commission
import com.example.trackpoints.data.model.Message
import com.example.trackpoints.data.model.MessageType
import com.example.trackpoints.data.model.Notification
import com.example.trackpoints.data.model.CommissionStatus
import com.example.trackpoints.data.model.User
import com.example.trackpoints.data.model.UserRole
import java.time.Instant

val dummyFreelancers = listOf(
    User(
        id = "f1",
        email = "jordan@design.com",
        role = UserRole.FREELANCER,
        fullName = "Jordan Smith",
        isApproved = true,
        createdAt = Instant.now().minusSeconds(1000000),
        lastSeen = Instant.now().minusSeconds(60), // Active 1 minute ago
        specialty = "Icon Design",
        photo = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1626785774573-4b799315345d?q=80&w=500",
            "https://images.unsplash.com/photo-1572044162444-ad60f128bde3?q=80&w=500"
        ),
        rating = 4.9f,
        totalProjects = 127,
        projectsTogether = 1,
        isRecent = true
    ),

    User(
        id = "f2",
        email = "sarah@creative.io",
        role = UserRole.FREELANCER,
        fullName = "Sarah Jenkins",
        isApproved = true,
        createdAt = Instant.now().minusSeconds(2000000),
        lastSeen = Instant.now().minusSeconds(7200), // Active 2 hours ago
        specialty = "Brand Identity",
        photo = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1558655146-d09347e92766?q=80&w=500",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=500"
        ),
        rating = 4.7f,
        totalProjects = 84,
        projectsTogether = 0,
        isRecent = true
    ),

    User(
        id = "f3",
        email = "marcus@ui.net",
        role = UserRole.FREELANCER,
        fullName = "Marcus Vaughn",
        isApproved = true,
        createdAt = Instant.now().minusSeconds(500000),
        lastSeen = Instant.now().minusSeconds(180), // Active 3 minutes ago
        specialty = "UI Illustration",
        photo = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1586717791821-3f44a563eb4c?q=80&w=500",
            "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?q=80&w=500"
        ),
        rating = 4.8f,
        totalProjects = 210,
        projectsTogether = 2,
        isRecent = false
    ),

    User(
        id = "f4",
        email = "elena@logos.com",
        role = UserRole.FREELANCER,
        fullName = "Elena Rodriguez",
        isApproved = true,
        createdAt = Instant.now().minusSeconds(3000000),
        lastSeen = Instant.now().minusSeconds(259200), // Active 3 days ago
        specialty = "Logo Design",
        photo = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1541462608141-ad4d3544b68c?q=80&w=500",
            "https://images.unsplash.com/photo-1629197520635-16570f00aa6a?q=80&w=500"
        ),
        rating = 5.0f,
        totalProjects = 45,
        projectsTogether = 1,
        isRecent = false
    ),

    User(
        id = "f5",
        email = "david@gameart.com",
        role = UserRole.FREELANCER,
        fullName = "David Chen",
        isApproved = true,
        createdAt = Instant.now().minusSeconds(1500000),
        lastSeen = Instant.now().minusSeconds(10), // Active 10 seconds ago
        specialty = "2D Game Assets",
        photo = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1614850523296-d8c1af93d400?q=80&w=500",
            "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=500"
        ),
        rating = 4.6f,
        totalProjects = 92,
        projectsTogether = 0,
        isRecent = false
    ),
    User(
        id = "f5",
        email = "david@gameart.com",
        role = UserRole.CLIENT,
        fullName = "Random 1 Bla Bla Bla Bla Bla Bla Bla Bla Bla Bla",
        isApproved = false,
        createdAt = Instant.now().minusSeconds(1500000),
        lastSeen = Instant.now().minusSeconds(10), // Active 10 seconds ago
        specialty = "2D Game Assets",
        photo = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200&h=200&auto=format&fit=crop",
        portfolio = listOf(
            "https://images.unsplash.com/photo-1614850523296-d8c1af93d400?q=80&w=500",
            "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=500"
        ),
        rating = 4.6f,
        totalProjects = 92,
        projectsTogether = 0,
        isRecent = false
    ),
)
val myId = "1b4a949a-e425-4a71-8361-f94ede447949"

//val dummyMessages: List<List<Message>> = listOf(
//    listOf(
//        Message(
//            "m1",
//            "f1",
//            "Hi! I saw your request for icon designs. Are you available to discuss?",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(3600)
//        ),
//        Message(
//            "m2",
//            myId,
//            "Yes! I love your portfolio. Do you have a starting rate?",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(3000)
//        ),
//        Message(
//            "m3",
//            "f1",
//            "Typically I charge per icon set, but we can do hourly if preferred!",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(2500)
//        )
//    ),
//
//    listOf(
//        Message(
//            "m4",
//            myId,
//            "Hey Sarah, how is the brand identity coming along?",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(7200)
//        ),
//        Message(
//            "m5",
//            "f2",
//            "Going great! I'm currently working on the color palette options.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(6000)
//        ),
//        Message(
//            "m6",
//            "f2",
//            "I should have the first draft ready by tomorrow morning.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(5500)
//        )
//    ),
//
//    listOf(
//        Message(
//            "m7",
//            "f3",
//            "I just finished the UI illustrations for the landing page.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(10000)
//        ),
//        Message(
//            "m8",
//            myId,
//            "Awesome. I'll take a look at them in the dashboard now.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(9500)
//        )
//    ),
//
//    listOf(
//        Message(
//            "m9",
//            "f4",
//            "Do you have any specific font preferences for the logo?",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(15000)
//        ),
//        Message(
//            "m10",
//            myId,
//            "I prefer something clean and sans-serif. Maybe something bold.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(14000)
//        ),
//        Message(
//            "m11",
//            "f4",
//            "Got it. I'll send over 3 variations using that style.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(13000)
//        )
//    ),
//
//    listOf(
//        Message(
//            "m12",
//            myId,
//            "Are the 2D assets ready for the game export?",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(600)
//        ),
//        Message(
//            "m13",
//            "f5",
//            "Almost there! Just polishing the sprite sheet animations.",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(300)
//        ),
//        Message(
//            "m14",
//            myId,
//            "Sounds good. No rush, quality is the priority!",
//            MessageType.TEXT,
//            null,
//            null,
//            null,
//            Instant.now().minusSeconds(100)
//        )
//    )
//)
//
//val dummyNotifications = listOf(
//    Notification(
//        id = "n1",
//        message = "Jordan Smith accepted your commission request for 'Icon Set Design'.",
//        isRead = false,
//        createdAt = Instant.now().minusSeconds(1800) // 30 minutes ago
//    ),
//    Notification(
//        id = "n2",
//        message = "Your payment for 'Brand Identity' has been successfully processed.",
//        isRead = true,
//        createdAt = Instant.now().minusSeconds(7200) // 2 hours ago
//    ),
//    Notification(
//        id = "n3",
//        message = "Sarah Jenkins sent you a new message regarding the project timeline.",
//        isRead = false,
//        createdAt = Instant.now().minusSeconds(14400) // 4 hours ago
//    ),
//    Notification(
//        id = "n4",
//        message = "Your portfolio has been approved! You can now apply for premium projects.",
//        isRead = true,
//        createdAt = Instant.now().minusSeconds(86400) // 1 day ago
//    ),
//    Notification(
//        id = "n5",
//        message = "Reminder: Your deadline for 'UI Illustrations' is in 24 hours.",
//        isRead = false,
//        createdAt = Instant.now().minusSeconds(90000) // ~1 day ago
//    ),
//    Notification(
//        id = "n6",
//        message = "Elena Rodriguez left you a 5-star review for your recent collaboration.",
//        isRead = true,
//        createdAt = Instant.now().minusSeconds(172800) // 2 days ago
//    )
//)
//
//val currentClient = User(
//    id = "1b4a949a-e425-4a71-8361-f94ede447949", // Your provided ID
//    email = "client.admin@example.com",
//    role = UserRole.CLIENT,
//    fullName = "Justine Casiano",
//    isApproved = true,
//    createdAt = Instant.now().minusSeconds(5000000),
//    photo = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=200&h=200&auto=format&fit=crop"
//)
//
//val dummyCommissions = listOf(
//    Commission(
//        id = "c1",
//        name = "Fintech Icon Set",
//        client = currentClient,
//        freelancer = dummyFreelancers[0], // Jordan Smith
//        status = CommissionStatus.IN_PROGRESS,
//        points = 250000,
//        description = "Design a set of 20 minimalist custom icons for a fintech mobile application.",
//        dueDate = Instant.now().plusSeconds(604800), // Due in 7 days
//        createdAt = Instant.now().minusSeconds(172800), // Created 2 days ago
//        messages = dummyMessages[0],
//        messageReadAt = true,
//        datePaid = null
//    ),
//
//    Commission(
//        id = "c2",
//        name = "Eco-Brand Identity",
//        client = currentClient,
//        freelancer = dummyFreelancers[1], // Sarah Jenkins
//        status = CommissionStatus.PENDING,
//        points = 5000000,
//        description = "Complete rebranding including logo, color palette, and social media templates.",
//        dueDate = Instant.now().plusSeconds(1209600), // Due in 14 days
//        createdAt = Instant.now().minusSeconds(86400), // Created 1 day ago
//        messages = dummyMessages[1],
//        messageReadAt = false,
//        datePaid = null
//    ),
//
//    Commission(
//        id = "c3",
//        name = "Landing Page Hero Illustration",
//        client = currentClient,
//        freelancer = dummyFreelancers[2], // Marcus Vaughn
//        status = CommissionStatus.DONE,
//        points = 12000,
//        description = "Hero section illustration for a landing page featuring a futuristic city.",
//        dueDate = Instant.now().minusSeconds(86400), // Was due yesterday
//        createdAt = Instant.now().minusSeconds(604800), // Created 1 week ago
//        messages = dummyMessages[2],
//        messageReadAt = true,
//        datePaid = Instant.now().minusSeconds(100000)
//    ),
//
//    Commission(
//        id = "c4",
//        name = "Coffee Shop Logo",
//        client = currentClient,
//        freelancer = dummyFreelancers[3], // Elena Rodriguez
//        status = CommissionStatus.IN_PROGRESS,
//        points = 3500,
//        description = "Custom typography logo design for an organic coffee brand.",
//        dueDate = Instant.now().plusSeconds(432000), // Due in 5 days
//        createdAt = Instant.now().minusSeconds(259200), // Created 3 days ago
//        messages = dummyMessages[3],
//        messageReadAt = true,
//        datePaid = null
//    ),
//
//    Commission(
//        id = "c5",
//        name = "2D Character Animation",
//        client = currentClient,
//        freelancer = dummyFreelancers[4], // David Chen
//        status = CommissionStatus.PENDING,
//        points = 800,
//        description = "Sprite sheet animation for a 2D character run cycle.",
//        dueDate = Instant.now().plusSeconds(259200), // Due in 3 days
//        createdAt = Instant.now().minusSeconds(3600), // Created 1 hour ago
//        messages = dummyMessages[4],
//        messageReadAt = null,
//        datePaid = null
//    )
//)
//
