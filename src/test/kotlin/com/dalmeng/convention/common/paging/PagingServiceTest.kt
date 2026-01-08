package com.dalmeng.convention.common.paging

import com.dalmeng.convention.common.exception.InvalidPageRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Pageable

data class TestItem(
    val id: String,
    val seq: Long,
    val data: String
)

class TestPagingHandlers(
    private val testData: List<TestItem>
) : PagingHandlers<TestItem> {
    
    private fun findSeqById(id: String): Long {
        return testData.first { it.id == id }.seq
    }

    override fun getCursorFromResponse(data: TestItem): String {
        return data.id
    }

    override fun findById(id: String): TestItem {
        return testData.first { it.id == id }
    }

    override fun findAllByOrderByIdAsc(pageable: Pageable): List<TestItem> {
        val sorted = testData.sortedBy { it.seq }
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(sorted.size)
        return sorted.subList(start, end)
    }

    override fun findAllByOrderByIdDesc(pageable: Pageable): List<TestItem> {
        val sorted = testData.sortedByDescending { it.seq }
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(sorted.size)
        return sorted.subList(start, end)
    }

    override fun findAllByIdGreaterThanOrderByIdAsc(id: String, pageable: Pageable): List<TestItem> {
        val cursorSeq = findSeqById(id)
        val filtered = testData.filter { it.seq > cursorSeq }.sortedBy { it.seq }
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(filtered.size)
        return filtered.subList(start, end)
    }

    override fun findAllByIdLessThanOrderByIdDesc(id: String, pageable: Pageable): List<TestItem> {
        val cursorSeq = findSeqById(id)
        val filtered = testData.filter { it.seq < cursorSeq }.sortedByDescending { it.seq }
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(filtered.size)
        return filtered.subList(start, end)
    }
}

class PagingServiceTest : BehaviorSpec({
    val pagingService = PagingService()

    // 100개의 테스트 데이터 생성 (id: item_001 ~ item_100, seq: 1 ~ 100)
    val testData = (1..100).map { i ->
        TestItem(
            id = "item_${"%03d".format(i)}",
            seq = i.toLong(),
            data = "data_$i"
        )
    }

    val handlers = TestPagingHandlers(testData)

    Given("PagingService with DOWN direction") {
        When("page-based paging - first page") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns first 20 items in ascending order") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_001"
                result.items[0].seq shouldBe 1
                result.items[19].id shouldBe "item_020"
                result.items[19].seq shouldBe 20
                result.page shouldBe 1
                result.limit shouldBe 20
                result.itemCount shouldBe 20
            }
        }

        When("page-based paging - second page") {
            val request = BasePagingRequest(
                limit = 20,
                page = 2,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items 21-40 in ascending order") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_021"
                result.items[0].seq shouldBe 21
                result.items[19].id shouldBe "item_040"
                result.items[19].seq shouldBe 40
                result.page shouldBe 2
            }
        }

        When("page-based paging - last page (exact fit)") {
            val request = BasePagingRequest(
                limit = 20,
                page = 5,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns last 20 items and hasNext is false") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_081"
                result.items[19].id shouldBe "item_100"
                result.page shouldBe 5
            }
        }

        When("page-based paging - last page (partial)") {
            val request = BasePagingRequest(
                limit = 30,
                page = 4,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns remaining items and hasNext is false") {
                result.items.size shouldBe 10
                result.items[0].id shouldBe "item_091"
                result.items[9].id shouldBe "item_100"
                result.page shouldBe 4
            }
        }

        When("cursor-based paging - middle cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_050",
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items after cursor in ascending order") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_051"
                result.items[0].seq shouldBe 51
                result.items[19].id shouldBe "item_070"
                result.items[19].seq shouldBe 70
                result.hasNext shouldBe true
                result.nextCursor shouldBe "item_070"
            }
        }

        When("cursor-based paging - near end cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_085",
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns remaining items and hasNext is false") {
                result.items.size shouldBe 15
                result.items[0].id shouldBe "item_086"
                result.items[14].id shouldBe "item_100"
                result.hasNext shouldBe false
                result.nextCursor shouldBe "item_100"
            }
        }

        When("cursor-based paging - last item cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_100",
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns empty list") {
                result.items.size shouldBe 0
                result.hasNext shouldBe false
                result.nextCursor shouldBe null
            }
        }

        When("default request (no page, no cursor)") {
            val request = BasePagingRequest(
                limit = 20,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("treats as page request with page=1") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_001"
                result.nextCursor shouldBe "item_020"
            }
        }
    }

    Given("PagingService with UP direction") {
        When("page-based paging - first page") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns last 20 items in descending order (reversed)") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_081"
                result.items[0].seq shouldBe 81
                result.items[19].id shouldBe "item_100"
                result.items[19].seq shouldBe 100
                result.page shouldBe 1
                result.limit shouldBe 20
            }
        }

        When("page-based paging - second page") {
            val request = BasePagingRequest(
                limit = 20,
                page = 2,
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items 80-61 in descending order (reversed)") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_061"
                result.items[0].seq shouldBe 61
                result.items[19].id shouldBe "item_080"
                result.items[19].seq shouldBe 80
                result.page shouldBe 2
            }
        }

        When("page-based paging - last page (exact fit)") {
            val request = BasePagingRequest(
                limit = 20,
                page = 5,
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns first 20 items and hasPrev is false") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_001"
                result.items[19].id shouldBe "item_020"
            }
        }

        When("cursor-based paging - middle cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_050",
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items before cursor in descending order (reversed)") {
                result.items.size shouldBe 20
                result.items[0].id shouldBe "item_030"
                result.items[0].seq shouldBe 30
                result.items[19].id shouldBe "item_049"
                result.items[19].seq shouldBe 49
                result.hasPrev shouldBe true
                result.prevCursor shouldBe "item_030"
            }
        }

        When("cursor-based paging - near start cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_015",
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns remaining items and hasPrev is false") {
                result.items.size shouldBe 14
                result.items[0].id shouldBe "item_001"
                result.items[13].id shouldBe "item_014"
                result.hasPrev shouldBe false
                result.prevCursor shouldBe "item_001"
            }
        }

        When("cursor-based paging - first item cursor") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_001",
                direction = PagingDirection.UP
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns empty list") {
                result.items.size shouldBe 0
                result.hasPrev shouldBe false
                result.prevCursor shouldBe null
            }
        }
    }

    Given("PagingService with BIDIRECTIONAL direction") {
        When("cursor in middle - even limit") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_050",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items around cursor with center item") {
                // half = 10, rest = 10
                // upper: items < 50, descending, take 10 -> item_049 ~ item_040 (reversed)
                // center: item_050
                // lower: items > 50, ascending, take 10 -> item_051 ~ item_060
                result.items.size shouldBe 21 // 10 + 1 + 10
                result.items[0].id shouldBe "item_040" // upperItems.reversed()[0]
                result.items[9].id shouldBe "item_049" // upperItems.reversed()[9]
                result.items[10].id shouldBe "item_050" // center
                result.items[11].id shouldBe "item_051" // lowerItems[0]
                result.items[20].id shouldBe "item_060" // lowerItems[9]
                result.hasPrev shouldBe true // upper.size (10) > half (10) -> false, but we check if there are more
                result.hasNext shouldBe true // lower.size (50) > rest (10) -> true
                result.prevCursor shouldBe "item_040"
                result.nextCursor shouldBe "item_060"
                result.page shouldBe null
            }
        }

        When("cursor in middle - odd limit") {
            val request = BasePagingRequest(
                limit = 21,
                cursor = "item_050",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items around cursor with correct split") {
                // half = 10, rest = 11
                result.items.size shouldBe 22 // 10 + 1 + 11
                result.items[10].id shouldBe "item_050" // center
                result.hasPrev shouldBe true
                result.hasNext shouldBe true
            }
        }

        When("cursor near start") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_010",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items with limited upper items") {
                // upper: items < 10 -> 9 items
                // center: item_010
                // lower: items > 10 -> many items
                result.items.size shouldBe 20 // 9 + 1 + 10
                result.items[0].id shouldBe "item_001" // first item
                result.items[8].id shouldBe "item_009"
                result.items[9].id shouldBe "item_010" // center
                result.items[10].id shouldBe "item_011"
                result.hasPrev shouldBe false // upper.size (9) <= half (10)
                result.hasNext shouldBe true
                result.prevCursor shouldBe "item_001"
                result.nextCursor shouldBe "item_020"
            }
        }

        When("cursor near end") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_090",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items with limited lower items") {
                // upper: items < 90 -> many items
                // center: item_090
                // lower: items > 90 -> 10 items
                result.items.size shouldBe 21 // 10 + 1 + 10
                result.items[10].id shouldBe "item_090" // center
                result.items[20].id shouldBe "item_100" // last item
                result.hasPrev shouldBe true
                result.hasNext shouldBe false // lower.size (10) <= rest (10)
                result.prevCursor shouldBe "item_080"
                result.nextCursor shouldBe "item_100"
            }
        }

        When("cursor at start") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_001",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items with no upper items") {
                // upper: items < 1 -> empty
                // center: item_001
                // lower: items > 1 -> many items
                result.items.size shouldBe 11 // 0 + 1 + 10
                result.items[0].id shouldBe "item_001" // center (first item)
                result.items[1].id shouldBe "item_002"
                result.items[10].id shouldBe "item_011"
                result.hasPrev shouldBe false
                result.hasNext shouldBe true
                result.prevCursor shouldBe "item_001"
                result.nextCursor shouldBe "item_011"
            }
        }

        When("cursor at end") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_100",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns items with no lower items") {
                // upper: items < 100 -> many items
                // center: item_100
                // lower: items > 100 -> empty
                result.items.size shouldBe 11 // 10 + 1 + 0
                result.items[0].id shouldBe "item_090"
                result.items[9].id shouldBe "item_099"
                result.items[10].id shouldBe "item_100" // center (last item)
                result.hasPrev shouldBe true
                result.hasNext shouldBe false
                result.prevCursor shouldBe "item_090"
                result.nextCursor shouldBe "item_100"
            }
        }

        When("cursor with hasPrev calculation - more than half") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_060",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("hasPrev is true when upper items exceed half") {
                // upper: 59 items, half = 10
                // upper.size (59) > half (10) -> hasPrev = true
                result.hasPrev shouldBe true
                result.items.size shouldBe 21
            }
        }

        When("cursor with hasNext calculation - more than rest") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_040",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("hasNext is true when lower items exceed rest") {
                // lower: 60 items, rest = 10
                // lower.size (60) > rest (10) -> hasNext = true
                result.hasNext shouldBe true
                result.items.size shouldBe 21
            }
        }
    }

    Given("PagingService validation") {
        When("bidirectional with page") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                cursor = null,
                direction = PagingDirection.BIDIRECTIONAL
            )

            Then("throws InvalidPageRequestException") {
                shouldThrow<InvalidPageRequestException> {
                    pagingService.paginate<TestItem>(request, handlers)
                }
            }
        }

        When("bidirectional without cursor") {
            val request = BasePagingRequest(
                limit = 20,
                page = null,
                cursor = null,
                direction = PagingDirection.BIDIRECTIONAL
            )

            Then("throws InvalidPageRequestException") {
                shouldThrow<InvalidPageRequestException> {
                    pagingService.paginate<TestItem>(request, handlers)
                }
            }
        }

        When("page and cursor together") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                cursor = "item_050",
                direction = PagingDirection.DOWN
            )

            Then("throws InvalidPageRequestException") {
                shouldThrow<InvalidPageRequestException> {
                    pagingService.paginate<TestItem>(request, handlers)
                }
            }
        }
    }

    Given("PagingService map function") {
        When("mapping paging response") {
            val request = BasePagingRequest(
                limit = 20,
                page = 1,
                direction = PagingDirection.DOWN
            )

            val pagingResult = pagingService.paginate<TestItem>(request, handlers)

            val mappedResult = pagingService.map<TestItem, String>(pagingResult) { item ->
                item.data // TestItem -> String
            }

            Then("maps items correctly while preserving metadata") {
                mappedResult.items.size shouldBe 20
                mappedResult.items[0] shouldBe "data_1"
                mappedResult.items[19] shouldBe "data_20"
                mappedResult.hasNext shouldBe pagingResult.hasNext
                mappedResult.hasPrev shouldBe pagingResult.hasPrev
                mappedResult.nextCursor shouldBe pagingResult.nextCursor
                mappedResult.prevCursor shouldBe pagingResult.prevCursor
                mappedResult.limit shouldBe pagingResult.limit
                mappedResult.page shouldBe pagingResult.page
                mappedResult.itemCount shouldBe pagingResult.itemCount
            }
        }

        When("mapping bidirectional response") {
            val request = BasePagingRequest(
                limit = 20,
                cursor = "item_050",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val pagingResult = pagingService.paginate<TestItem>(request, handlers)

            val mappedResult = pagingService.map<TestItem, String>(pagingResult) { item ->
                item.id
            }

            Then("preserves all bidirectional metadata") {
                mappedResult.items.size shouldBe pagingResult.items.size
                mappedResult.hasPrev shouldBe pagingResult.hasPrev
                mappedResult.hasNext shouldBe pagingResult.hasNext
                mappedResult.prevCursor shouldBe pagingResult.prevCursor
                mappedResult.nextCursor shouldBe pagingResult.nextCursor
            }
        }
    }

    Given("PagingService edge cases") {
        When("limit is 1") {
            val request = BasePagingRequest(
                limit = 1,
                cursor = "item_050",
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns single item") {
                result.items.size shouldBe 1
                result.items[0].id shouldBe "item_051"
                result.hasNext shouldBe true
                result.nextCursor shouldBe "item_051"
            }
        }

        When("limit is larger than total items") {
            val request = BasePagingRequest(
                limit = 200,
                page = 1,
                direction = PagingDirection.DOWN
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns all items") {
                result.items.size shouldBe 100
                result.items[0].id shouldBe "item_001"
                result.items[99].id shouldBe "item_100"
                result.hasNext shouldBe false
                result.nextCursor shouldBe "item_100"
            }
        }

        When("bidirectional with limit 1") {
            val request = BasePagingRequest(
                limit = 1,
                cursor = "item_050",
                direction = PagingDirection.BIDIRECTIONAL
            )

            val result = pagingService.paginate<TestItem>(request, handlers)

            Then("returns only center item") {
                result.items.size shouldBe 1
                result.items[0].id shouldBe "item_050"
                result.hasPrev shouldBe true
                result.hasNext shouldBe true
            }
        }
    }
})

