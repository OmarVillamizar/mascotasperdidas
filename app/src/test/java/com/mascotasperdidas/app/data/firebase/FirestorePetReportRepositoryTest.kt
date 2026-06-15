package com.mascotasperdidas.app.data.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mascotasperdidas.app.data.image.Base64ImageCodec
import com.mascotasperdidas.app.data.image.ImageCompressor
import com.mascotasperdidas.app.data.image.ImageTooLargeException
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class FirestorePetReportRepositoryTest {

    private val firestore: FirebaseFirestore = mockk()
    private val auth: FirebaseAuth = mockk()
    private val imageCompressor: ImageCompressor = mockk()

    private val firebaseUser: FirebaseUser = mockk()
    private val collection: CollectionReference = mockk()
    private val docRef: DocumentReference = mockk()

    private lateinit var repo: FirestorePetReportRepository

    private val sampleReport = PetReport(
        id = "",
        ownerUid = "uid-test",
        ownerInitial = "T",
        ownerName = "Test User",
        petName = "Firulais",
        type = ReportType.LOST,
        breed = "Mestizo",
        description = "Perro blanco con manchas negras",
        location = "Parque Central, Cúcuta",
        imageUrl = "",
        recencyLabel = "RECIENTE",
        createdAtEpochMs = 1_000_000L,
    )

    @Before
    fun setUp() {
        repo = FirestorePetReportRepository(firestore, auth, imageCompressor)

        // Default: authenticated user
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid-test"

        // Default Firestore collection / doc wiring
        every { firestore.collection("pet_reports") } returns collection
        every { collection.document() } returns docRef
        every { docRef.set(any()) } returns Tasks.forResult(null)
    }

    // ── TASK-6 Test 1: photo stored as base64 ──────────────────────────────

    @Test
    fun createReport_withPhoto_storesBase64InImageUrl() = runTest {
        val rawBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val compressedBytes = byteArrayOf(0x10, 0x11, 0x12)
        val expectedBase64 = Base64ImageCodec.encode(compressedBytes)

        every { imageCompressor.compressToJpeg(rawBytes) } returns compressedBytes

        val dtoSlot = slot<Any>()
        every { docRef.set(capture(dtoSlot)) } returns Tasks.forResult(null)

        repo.createReport(sampleReport, listOf(rawBytes))

        val capturedDto = dtoSlot.captured
        assertNotNull("DTO must have been set on Firestore", capturedDto)
        // Verify the DTO carries the expected base64 value
        val dtoClass = capturedDto.javaClass
        val imageUrlField = dtoClass.declaredFields.find { it.name == "imageUrl" }
        assertNotNull("DTO must have imageUrl field", imageUrlField)
        imageUrlField!!.isAccessible = true
        assertEquals(expectedBase64, imageUrlField.get(capturedDto) as String)
    }

    // ── TASK-6 Test 2: size budget exceeded → throws, no Firestore write ───

    @Test
    fun createReport_exceedsBudget_throwsImageTooLargeException() = runTest {
        val rawBytes = byteArrayOf(0x01, 0x02)
        // Return a byte array that when encoded exceeds MAX_DOC_BASE64_BYTES
        // base64 length ≈ ceil(n/3)*4. We need encoded length > 921_600.
        // With encode (no padding), each 3 bytes → 4 chars. So ~921_600 / 4 * 3 ≈ 691_200 bytes needed.
        val oversizedCompressed = ByteArray(700_000) { it.toByte() }
        val oversizedBase64 = Base64ImageCodec.encode(oversizedCompressed)
        assert(Base64ImageCodec.exceedsBudget(oversizedBase64)) {
            "Test setup: oversized base64 must exceed budget"
        }

        every { imageCompressor.compressToJpeg(rawBytes) } returns oversizedCompressed

        assertThrows(ImageTooLargeException::class.java) {
            kotlinx.coroutines.runBlocking { repo.createReport(sampleReport, listOf(rawBytes)) }
        }

        // Firestore set must NOT have been called
        verify(exactly = 0) { docRef.set(any()) }
    }

    // ── TASK-6 Test 3: empty image list keeps existing imageUrl ────────────

    @Test
    fun createReport_emptyImageList_keepsExistingImageUrl() = runTest {
        val reportWithUrl = sampleReport.copy(imageUrl = "https://placedog.net/400/300?id=1")

        val dtoSlot = slot<Any>()
        every { docRef.set(capture(dtoSlot)) } returns Tasks.forResult(null)

        repo.createReport(reportWithUrl, emptyList())

        verify(exactly = 0) { imageCompressor.compressToJpeg(any()) }

        val capturedDto = dtoSlot.captured
        val imageUrlField = capturedDto.javaClass.declaredFields.find { it.name == "imageUrl" }!!
        imageUrlField.isAccessible = true
        assertEquals(
            "imageUrl must remain unchanged when no image bytes provided",
            "https://placedog.net/400/300?id=1",
            imageUrlField.get(capturedDto) as String,
        )
    }

    // ── TASK-6 Test 4: unauthenticated user → throws IllegalStateException ─

    @Test
    fun createReport_nullUid_throwsIllegalState() = runTest {
        every { auth.currentUser } returns null

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                repo.createReport(sampleReport, listOf(byteArrayOf(0x01)))
            }
        }
    }

    // ── TASK-6 Test 5: multiple photos → compressor called once, extras dropped

    @Test
    fun createReport_withMultiplePhotos_dropsExtras_storesPrimaryOnly() = runTest {
        val bytes1 = byteArrayOf(0x01, 0x02)
        val bytes2 = byteArrayOf(0x03, 0x04)
        val bytes3 = byteArrayOf(0x05, 0x06)
        val compressedPrimary = byteArrayOf(0x10, 0x11)

        every { imageCompressor.compressToJpeg(bytes1) } returns compressedPrimary

        val dtoSlot = slot<Any>()
        every { docRef.set(capture(dtoSlot)) } returns Tasks.forResult(null)

        repo.createReport(sampleReport, listOf(bytes1, bytes2, bytes3))

        // Compressor called exactly once (only for the primary image)
        verify(exactly = 1) { imageCompressor.compressToJpeg(any()) }

        // additionalPhotos must be empty
        val capturedDto = dtoSlot.captured
        val additionalField = capturedDto.javaClass.declaredFields
            .find { it.name == "additionalPhotos" }!!
        additionalField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val additionalPhotos = additionalField.get(capturedDto) as List<*>
        assertEquals("additionalPhotos must be empty for MVP", 0, additionalPhotos.size)
    }

    // ── TASK-6 Test 6: Firestore throws → exception propagates ─────────────

    @Test
    fun createReport_firestoreThrows_propagatesException() = runTest {
        val rawBytes = byteArrayOf(0x01, 0x02)
        val compressedBytes = byteArrayOf(0x10)

        every { imageCompressor.compressToJpeg(rawBytes) } returns compressedBytes
        every { docRef.set(any()) } returns Tasks.forException(
            RuntimeException("Firestore write failed"),
        )

        assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.runBlocking { repo.createReport(sampleReport, listOf(rawBytes)) }
        }
    }
}
