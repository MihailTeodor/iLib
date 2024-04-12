package it.gurzu.SWAM.iLib.modelTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

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
		Exception thrownException = assertThrows(IllegalArgumentException.class, ()->{
			new FakeBaseEntity(null);
		});
		assertEquals("uuid cannot be null!", thrownException.getMessage());	
	}
	
	@Test
	public void testEquals() {
		assertEquals(entity1, entity1);
		assertNotEquals(entity1, entity2);
	}
}
