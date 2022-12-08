package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.element.*;

public class ParticleSystemLoaderTest {
	private ParticleSystemLoader loader;

	@BeforeEach
	void before() {
		loader = new ParticleSystemLoader(new Randomiser());
	}

	@Test
	void load() throws IOException {
		final String xml = """
			<sparks>
			    <max>50</max>
			    <policy>
			        <increment>10</increment>
			    </policy>
			    <lifetime>5s</lifetime>
			    <position>
			        <origin />
			    </position>
			    <vector>
			        <cone>
			            <normal>Y</normal>
			            <radius>1</radius>
			        </cone>
			    </vector>
			    <colour>
			        <interpolated>
			            <start>1, 1, 0.5, 1</start>
			            <end>0.5, 0, 0, 0</end>
			        </interpolated>
			    </colour>
			    <!--
			    <influences>
			        <literal>-Y</literal>
			    </influences>
			    -->
			    <surfaces>
			        <reflect>
			            <plane>
			                <normal>Y</normal>
			                <distance>0</distance>
			            </plane>
			            <absorb>0.3</absorb>
			        </reflect>
			    </surfaces>
			</sparks>
		""";

		final Element root = new ElementLoader().load(new StringReader(xml));
		final ParticleSystem sys = loader.load(root);
		assertNotNull(sys);
		assertEquals(50, sys.max());
		assertEquals(5000L, sys.lifetime());
	}
}
