package org.sarge.jove.platform.vulkan.core;

class QueryTest {
//	private DeviceContext device;
//
//	@BeforeEach
//	void before() {
//		device = new MockDeviceContext();
//	}
//
//	@Nested
//	class OcclusionQueryTest {
//		private Pool pool;
//
//		@BeforeEach
//		void before() {
//			pool = Pool.occlusion(device, 1);
//		}
//
//		@Test
//		void query() {
//			final Query query = pool.query(0);
//			final Command begin = query.begin(VkQueryControlFlag.PRECISE);
//			final Command end = query.end();
//			pool.reset();
//			begin.execute(null, null);
//			end.execute(null, null);
//			// TODO - result
//		}
//
//		@Test
//		void timestamp() {
//			assertThrows(UnsupportedOperationException.class, () -> pool.timestamp(0, VkPipelineStage.FRAGMENT_SHADER));
//		}
//	}
//
//	@Nested
//	class TimestampTest {
//		private Pool pool;
//
//		@BeforeEach
//		void before() {
//			pool = new Pool(device, VkQueryType.TIMESTAMP, 1);
//		}
//
//		@Test
//		void timestamp() {
//			final Command timestamp = pool.timestamp(0, VkPipelineStage.FRAGMENT_SHADER);
//			pool.reset();
//			timestamp.execute(null, null);
//			// TODO - result
//		}
//
//		@Test
//		void query() {
//			assertThrows(UnsupportedOperationException.class, () -> pool.query(0));
//		}
//	}
//
//	@Nested
//	class StatisticsTest {
//		private Pool pool;
//
//		@BeforeEach
//		void before() {
//			pool = Pool.statistics(device, 1, Set.of(VkQueryPipelineStatisticFlag.INPUT_ASSEMBLY_VERTICES));
//		}
//
//		@Test
//		void query() {
//			final Query query = pool.query(0);
//			final Command begin = query.begin(VkQueryControlFlag.PRECISE);
//			final Command end = query.end();
//			pool.reset();
//			begin.execute(null, null);
//			end.execute(null, null);
//			// TODO - result
//		}
//
//		@Test
//		void timestamp() {
//			assertThrows(UnsupportedOperationException.class, () -> pool.timestamp(0, VkPipelineStage.FRAGMENT_SHADER));
//		}
//	}
}
