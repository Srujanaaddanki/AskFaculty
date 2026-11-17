package com.example.askfaculty

import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentDomainTest {

    @Test
    fun studentUser_initializationAndRoles_areCorrect() {
        val student = User(
            uid = "student_101",
            email = "22cs104@institution.edu",
            name = "Alex Carter",
            role = Constants.ROLE_STUDENT,
            studentId = "22CS104",
            department = "Computer Science",
            academicYear = "3",
            semester = "6",
            joinedCommunities = listOf("comm_1", "comm_2"),
            bookmarks = listOf("doubt_42"),
        )

        assertEquals("student_101", student.uid)
        assertEquals("Alex Carter", student.displayName)
        assertEquals(Constants.ROLE_STUDENT, student.role)
        assertEquals("22CS104", student.studentId)
        assertEquals(2, student.joinedCommunities.size)
        assertTrue(student.joinedCommunities.contains("comm_1"))
        assertTrue(student.bookmarks.contains("doubt_42"))
    }

    @Test
    fun doubtModel_codeSnippetAndResolution_fieldsWorkCorrectly() {
        val doubt = Doubt(
            id = "doubt_001",
            studentId = "student_101",
            studentName = "Alex Carter",
            studentIdNumber = "22CS104",
            facultyId = "fac_30456",
            facultyName = "Gowthami",
            subject = "Data Structures",
            topic = "AVL Tree Rotations",
            question = "Why does LR rotation require a double rotation?",
            codeSnippet = "void rotateLeft(Node* node) { ... }",
            codeLanguage = "cpp",
            status = Constants.STATUS_ANSWERED,
            isPrivate = false,
            helpfulCount = 5,
        )

        assertEquals(Constants.STATUS_ANSWERED, doubt.status)
        assertEquals("cpp", doubt.codeLanguage)
        assertFalse(doubt.isPrivate)
        assertEquals(5, doubt.helpfulCount)
        assertTrue(doubt.codeSnippet.isNotBlank())
    }

    @Test
    fun communityModel_membershipAndPosts_behaveCorrectly() {
        val community = Community(
            id = "comm_ds",
            name = "Advanced Algorithms & Data Structures",
            subject = "Data Structures",
            facultyId = "30456",
            facultyName = "Gowthami",
            members = listOf("student_1", "student_2", "student_3"),
            memberCount = 3,
            postCount = 14,
        )

        assertEquals(3, community.memberCount)
        assertTrue(community.members.contains("student_2"))
        assertFalse(community.members.contains("student_999"))
    }

    @Test
    fun resourceModel_pinnedAndDocumentTypes_validateProperly() {
        val resource = Resource(
            id = "res_001",
            subject = "Computer Networks",
            title = "OSI vs TCP/IP Protocol Suite",
            fileName = "computer_networks_lecture_notes.pdf",
            fileType = "application/pdf",
            uploaderName = "Gowthami",
            isPinned = true,
        )

        assertTrue(resource.isPinned)
        assertTrue(resource.fileName.endsWith(".pdf"))
        assertEquals("Computer Networks", resource.subject)
    }

    @Test
    fun doubtStatusWorkflow_transitionsCorrectly() {
        val initial = Constants.STATUS_PENDING
        assertEquals("PENDING", initial)

        val inProgress = Constants.STATUS_IN_PROGRESS
        assertEquals("IN_PROGRESS", inProgress)

        val answered = Constants.STATUS_ANSWERED
        assertEquals("ANSWERED", answered)

        val resolved = Constants.STATUS_RESOLVED
        assertEquals("RESOLVED", resolved)
    }
}
