package it.gurzu.SWAM.iLib.modelTest;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BaseEntityTest {
	private FakeBaseEntity entity1;
	private FakeBaseEntity entity2;
	
	@BeforeEach
	public void setup() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		entity1 = new FakeBaseEntity(uuid1);
		entity2 = new FakeBaseEntity(uuid2);
	}
	
	@Test
	public void testNullUUID() {
		Assertions.assertThrows(IllegalArgumentException.class, ()->{
			new FakeBaseEntity(null);
		});
	}
	
	@Test
	public void testEquals() {
		Assertions.assertEquals(entity1, entity1);
		Assertions.assertNotEquals(entity1, entity2);
	}
}
