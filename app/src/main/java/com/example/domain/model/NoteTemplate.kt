package com.example.domain.model

data class NoteTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val defaultTitle: String,
    val defaultContent: String,
    val defaultChecklist: List<ChecklistItem> = emptyList(),
    val defaultColor: Long = 0L,
    val defaultType: NoteType = NoteType.STANDARD,
    val defaultTags: List<String> = emptyList()
) {
    companion object {
        val ALL = listOf(
            NoteTemplate(
                id = "meeting",
                name = "Meeting Notes",
                icon = "groups",
                defaultTitle = "Meeting Notes",
                defaultContent = "# Objective\nBrief description of meeting goals.\n\n" +
                        "## Attendees\n• Name 1\n• Name 2\n\n" +
                        "## Discussion Notes\nKey points covered during the meeting.\n\n" +
                        "## Decisions Made\n1. Agreed on next steps.",
                defaultChecklist = listOf(
                    ChecklistItem(text = "Send summary email to team", isChecked = false),
                    ChecklistItem(text = "Schedule follow-up review", isChecked = false)
                ),
                defaultColor = 0xFF4F46E5,
                defaultType = NoteType.WORK,
                defaultTags = listOf("Meeting", "Work")
            ),
            NoteTemplate(
                id = "journal",
                name = "Daily Journal",
                icon = "book",
                defaultTitle = "Daily Journal",
                defaultContent = "# Gratitude\n3 things I am grateful for today:\n1. \n2. \n3. \n\n" +
                        "# Daily Focus\nWhat is the most important outcome today?\n\n" +
                        "# Evening Reflection\nWins, learnings, and thoughts.",
                defaultColor = 0xFFF59E0B,
                defaultType = NoteType.PERSONAL,
                defaultTags = listOf("Journal", "Daily")
            ),
            NoteTemplate(
                id = "project",
                name = "Project Plan",
                icon = "rocket_launch",
                defaultTitle = "Project Roadmap",
                defaultContent = "# Project Scope & Vision\nOverview of deliverables.\n\n" +
                        "## Tech Architecture\n• Jetpack Compose & M3\n• Room + Cloud Sync\n\n" +
                        "## Deadlines\nPhase 1: End of week\nPhase 2: Next sprint",
                defaultChecklist = listOf(
                    ChecklistItem(text = "Define data models & Room schemas", isChecked = true),
                    ChecklistItem(text = "Implement ViewModels and UI screens", isChecked = false),
                    ChecklistItem(text = "Run end-to-end tests and deployment", isChecked = false)
                ),
                defaultColor = 0xFF0D9488,
                defaultType = NoteType.WORK,
                defaultTags = listOf("Project", "Productivity")
            ),
            NoteTemplate(
                id = "recipe",
                name = "Recipe",
                icon = "restaurant",
                defaultTitle = "New Recipe",
                defaultContent = "Prep Time: 15 mins | Cook Time: 30 mins | Servings: 4\n\n" +
                        "# Instructions\n1. Prep ingredients.\n2. Heat pan and cook evenly.\n3. Garnish and serve warm!",
                defaultChecklist = listOf(
                    ChecklistItem(text = "Olive oil (2 tbsp)", isChecked = false),
                    ChecklistItem(text = "Fresh garlic & herbs", isChecked = false),
                    ChecklistItem(text = "Salt & black pepper", isChecked = false)
                ),
                defaultColor = 0xFFEF4444,
                defaultType = NoteType.PERSONAL,
                defaultTags = listOf("Recipe", "Food")
            ),
            NoteTemplate(
                id = "habits",
                name = "Habit Tracker",
                icon = "task_alt",
                defaultTitle = "Weekly Habits",
                defaultContent = "Track daily progress towards your health, reading, and learning goals.",
                defaultChecklist = listOf(
                    ChecklistItem(text = "Drink 2L of water", isChecked = false),
                    ChecklistItem(text = "Read 20 pages", isChecked = false),
                    ChecklistItem(text = "30 mins exercise / walk", isChecked = false),
                    ChecklistItem(text = "Meditate 10 mins", isChecked = false)
                ),
                defaultColor = 0xFF8B5CF6,
                defaultType = NoteType.CHECKLIST,
                defaultTags = listOf("Habits", "Productivity")
            )
        )
    }
}
