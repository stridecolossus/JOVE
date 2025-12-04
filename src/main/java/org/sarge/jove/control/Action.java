package org.sarge.jove.control;

public record Action<E>(String name, Class<E> type) {

}
