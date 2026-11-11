package com.example.features.notices.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.notices.domain.NoticeCategory
import com.example.features.notices.domain.NoticePriority
import com.example.features.notices.domain.NoticeTargetAudience
import com.example.features.notices.domain.SchoolNotice
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NoticeRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("NoticeRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getNotices(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        category: NoticeCategory? = null,
        targetAudience: NoticeTargetAudience? = null,
        priority: NoticePriority? = null
    ): List<SchoolNotice> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.noticesPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededNotices(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    SchoolNotice.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterNotices(list, category, targetAudience, priority)
        } catch (e: Exception) {
            Log.w("NoticeRepository", "Error retrieving notices from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededNotices(schoolId)
            filterNotices(seeded, category, targetAudience, priority)
        }
    }

    private fun filterNotices(
        notices: List<SchoolNotice>,
        category: NoticeCategory?,
        targetAudience: NoticeTargetAudience?,
        priority: NoticePriority?
    ): List<SchoolNotice> {
        return notices
            .filter { not ->
                (category == null || not.category == category) &&
                (targetAudience == null || not.targetAudience == targetAudience || not.targetAudience == NoticeTargetAudience.ALL) &&
                (priority == null || not.priority == priority)
            }
            .sortedWith(
                compareByDescending<SchoolNotice> { it.isPinned }
                    .thenByDescending { it.priority == NoticePriority.URGENT }
                    .thenByDescending { it.createdAt }
            )
    }

    suspend fun saveNotice(notice: SchoolNotice): Result<SchoolNotice> {
        return try {
            TenantPathProvider.verifyTenantBoundary(notice.schoolId)
            val path = TenantPathProvider.noticesPath(notice.schoolId)
            db.collection(path).document(notice.id).set(notice.toFirestoreMap()).await()
            Result.success(notice)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteNotice(schoolId: String, noticeId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.noticesPath(schoolId)
            db.collection(path).document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededNotices(schoolId: String): List<SchoolNotice> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                SchoolNotice(
                    id = "not_oxf_01",
                    schoolId = schoolId,
                    title = "Midterm Examination Series Instructions & Timetable Release",
                    content = "The Office of the Controller of Examinations has published the definitive Midterm Schedule for Grades 9 through 12. Candidate hall tickets can now be generated and verified through the student portal. Non-programmable scientific calculators are permitted for STEM subjects.",
                    category = NoticeCategory.EXAMINATION,
                    priority = NoticePriority.IMPORTANT,
                    targetAudience = NoticeTargetAudience.ALL,
                    authorId = "usr_admin_01",
                    authorName = "Controller of Examinations",
                    publishedDate = "2026-03-22",
                    isPinned = true,
                    attachmentTitle = "Official_Midterm_Handbook_2026.pdf"
                ),
                SchoolNotice(
                    id = "not_oxf_02",
                    schoolId = schoolId,
                    title = "Annual Science Exhibition & Robotics Fair 2026",
                    content = "Oxford Academy will host the Inter-School Science Fair on May 15. All Grade 10 and 11 laboratory students participating in robotics, computational physics, and green chemistry must register project abstracts with Dr. Sarah Jenkins by April 10.",
                    category = NoticeCategory.EVENT,
                    priority = NoticePriority.NORMAL,
                    targetAudience = NoticeTargetAudience.STUDENTS,
                    authorId = "tea_oxf_01",
                    authorName = "Dr. Sarah Jenkins",
                    publishedDate = "2026-03-20",
                    isPinned = false,
                    attachmentTitle = "Science_Fair_Project_Rubric.pdf"
                ),
                SchoolNotice(
                    id = "not_oxf_03",
                    schoolId = schoolId,
                    title = "Urgent: Parent-Teacher Academic Conference Scheduling",
                    content = "Parent-Teacher Academic Conferences will occur on Friday from 1:00 PM to 5:00 PM. Parents are encouraged to book consultation slots via the institution communications portal to review midterm preparation and fee settlements.",
                    category = NoticeCategory.ACADEMIC,
                    priority = NoticePriority.URGENT,
                    targetAudience = NoticeTargetAudience.PARENTS,
                    authorId = "usr_admin_01",
                    authorName = "Principal's Secretariat",
                    publishedDate = "2026-03-24",
                    isPinned = true,
                    attachmentTitle = "Parent_Conference_Schedule.pdf"
                )
            )
        } else {
            listOf(
                SchoolNotice(
                    id = "not_cam_01",
                    schoolId = schoolId,
                    title = "Michaelmas Term Examinations & Cambridge Tripos Preparation",
                    content = "Preparatory guidelines for Year 10 Classical Mechanics and British Literature are now circulating in the departmental libraries. Attendance at morning preparatory sessions is compulsory for all candidates.",
                    category = NoticeCategory.EXAMINATION,
                    priority = NoticePriority.IMPORTANT,
                    targetAudience = NoticeTargetAudience.ALL,
                    authorId = "tea_cam_01",
                    authorName = "Dr. Isaac Newton",
                    publishedDate = "2026-03-22",
                    isPinned = true
                )
            )
        }
    }
}
