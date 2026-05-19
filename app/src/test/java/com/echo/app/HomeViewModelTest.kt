package com.echo.app

import com.echo.app.data.db.EchoRecord
import com.echo.app.data.model.ECHO_EMOJIS
import com.echo.app.data.repository.EchoRepository
import com.echo.app.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest {
        val repo = FakeRepository()
        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isTodayRecorded)
        assertNull(state.selectedEmoji)
        assertEquals(false, state.isSaving)
    }

    @Test
    fun testSelectEmoji() = runTest {
        val repo = FakeRepository()
        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val emoji = ECHO_EMOJIS.first()
        viewModel.selectEmoji(emoji)

        assertEquals(emoji, viewModel.uiState.value.selectedEmoji)
    }

    @Test
    fun testSaveRecord() = runTest {
        val repo = FakeRepository()
        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val emoji = ECHO_EMOJIS.first()
        viewModel.selectEmoji(emoji)
        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isTodayRecorded)
        assertNull(viewModel.uiState.value.selectedEmoji)
    }

    @Test
    fun testCannotSelectWhenAlreadyRecorded() = runTest {
        val existingRecord = EchoRecord(
            id = 1,
            emoji = "😊",
            label = "开心",
            dateMillis = FakeRepository().todayDateMillis(),
            createdAtMillis = System.currentTimeMillis()
        )
        val repo = FakeRepository(existingRecord)
        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isTodayRecorded)

        val emoji = ECHO_EMOJIS[1]
        viewModel.selectEmoji(emoji)

        assertNull(viewModel.uiState.value.selectedEmoji)
    }

    @Test
    fun testSaveDisabledWithoutSelection() = runTest {
        val repo = FakeRepository()
        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isTodayRecorded)
    }
}

class FakeRepository(initialRecord: EchoRecord? = null) : EchoRepository(
    dao = FakeDao()
) {
    private val _allRecords = MutableStateFlow(
        if (initialRecord != null) listOf(initialRecord) else emptyList()
    )
    private val _todayRecord = MutableStateFlow(initialRecord)

    override fun todayDateMillis(): Long = 1000000L

    override suspend fun getTodayRecord(): EchoRecord? = _todayRecord.value

    override fun observeTodayRecord() = _todayRecord

    override suspend fun saveRecord(emoji: String, label: String): Long {
        val record = EchoRecord(
            emoji = emoji,
            label = label,
            dateMillis = todayDateMillis(),
            createdAtMillis = System.currentTimeMillis()
        )
        _allRecords.value = listOf(record)
        _todayRecord.value = record
        return 1L
    }

    override fun observeAll() = _allRecords

    override fun observeMonth(year: Int, month: Int) = _allRecords

    override fun observeRange(startMillis: Long, endMillis: Long) = flowOf(
        _allRecords.value.filter { it.dateMillis in startMillis..endMillis }
    )

    override suspend fun getOldestRecord(): EchoRecord? = null
    override suspend fun getTotalCount(): Int = _allRecords.value.size
    override suspend fun deleteAll() {
        _allRecords.value = emptyList()
        _todayRecord.value = null
    }
}

class FakeDao : com.echo.app.data.db.EchoRecordDao {
    private val records = mutableListOf<EchoRecord>()

    override suspend fun insert(record: EchoRecord): Long {
        records.add(record.copy(id = records.size + 1L))
        return records.last().id
    }

    override suspend fun getByDate(dateMillis: Long): EchoRecord? {
        return records.find { it.dateMillis == dateMillis }
    }

    override fun observeByDate(dateMillis: Long) = flowOf(
        records.find { it.dateMillis == dateMillis }
    )

    override fun observeAll() = MutableStateFlow(records.toList())

    override fun observeRange(startMillis: Long, endMillis: Long) = flowOf(
        records.filter { it.dateMillis in startMillis..endMillis }
    )

    override suspend fun getOldest(): EchoRecord? = records.minByOrNull { it.dateMillis }

    override suspend fun count(): Int = records.size

    override suspend fun deleteAll() { records.clear() }
}
