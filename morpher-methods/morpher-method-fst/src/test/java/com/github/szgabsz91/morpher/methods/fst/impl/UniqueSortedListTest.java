package com.github.szgabsz91.morpher.methods.fst.impl;

import com.github.szgabsz91.morpher.core.model.WordPair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UniqueSortedListTest {

    @Test
    public void testConstructor() {
        List<String> list = new UniqueSortedList<>(comparingInt(String::length));
        list.addAll(List.of("aaa", "bb", "c", "c"));
        assertThat(list).hasSize(3);
        assertThat(list.get(0)).isEqualTo("c");
        assertThat(list.get(1)).isEqualTo("bb");
        assertThat(list.get(2)).isEqualTo("aaa");
    }

    @Test
    public void testAddWithEmptyList() {
        List<String> list = new UniqueSortedList<>(comparingInt(String::length));
        boolean result = list.add("a");
        assertThat(result).isTrue();
        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isEqualTo("a");
    }

    @Test
    public void testAddWithNonExistentNewItems() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        list.addAll(Set.of(10, 20, 30));
        boolean result = list.add(25);
        assertThat(result).isTrue();
        result = list.add(35);
        assertThat(result).isTrue();
        assertThat(list).hasSize(5);
        assertThat(list.get(0)).isEqualTo(10);
        assertThat(list.get(1)).isEqualTo(20);
        assertThat(list.get(2)).isEqualTo(25);
        assertThat(list.get(3)).isEqualTo(30);
        assertThat(list.get(4)).isEqualTo(35);
    }

    @Test
    public void testAddWithExistentNewItems() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        list.addAll(Set.of(10, 20, 30));
        boolean result = list.add(20);
        assertThat(result).isFalse();
        result = list.add(30);
        assertThat(result).isFalse();
        assertThat(list).hasSize(3);
        assertThat(list.get(0)).isEqualTo(10);
        assertThat(list.get(1)).isEqualTo(20);
        assertThat(list.get(2)).isEqualTo(30);
    }

    @Test
    public void testAddWithExistingNewItemsAccordingToComparator() {
        List<WordPair> list = new UniqueSortedList<>(comparing(WordPair::getLeftWord));
        list.addAll(Set.of(
                WordPair.of("a", "b"),
                WordPair.of("c", "d")
        ));
        boolean result = list.add(WordPair.of("c", "e"));
        assertThat(result).isFalse();
        assertThat(list).hasSize(2);
        assertThat(list.get(0)).isEqualTo(WordPair.of("a", "b"));
        assertThat(list.get(1)).isEqualTo(WordPair.of("c", "d"));
    }

    @Test
    public void testAddAllWithNonExistentNewItems() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        list.addAll(Set.of(10, 20, 30));
        boolean result = list.addAll(Set.of(25, 30));
        assertThat(result).isTrue();
        assertThat(list).hasSize(4);
        assertThat(list.get(0)).isEqualTo(10);
        assertThat(list.get(1)).isEqualTo(20);
        assertThat(list.get(2)).isEqualTo(25);
        assertThat(list.get(3)).isEqualTo(30);
    }

    @Test
    public void testAddAllWithExistingNewItems() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        list.addAll(Set.of(10, 20, 30));
        boolean result = list.addAll(Set.of(20, 30));
        assertThat(result).isFalse();
        assertThat(list).hasSize(3);
        assertThat(list.get(0)).isEqualTo(10);
        assertThat(list.get(1)).isEqualTo(20);
        assertThat(list.get(2)).isEqualTo(30);
    }

    @Test
    public void testAddAllWithExistingNewItemsAccordingToComparator() {
        List<WordPair> list = new UniqueSortedList<>(comparing(WordPair::getLeftWord));
        list.addAll(Set.of(
                WordPair.of("a", "b"),
                WordPair.of("c", "d")
        ));
        boolean result = list.addAll(Set.of(
                WordPair.of("c", "e")
        ));
        assertThat(result).isFalse();
        assertThat(list).hasSize(2);
        assertThat(list.get(0)).isEqualTo(WordPair.of("a", "b"));
        assertThat(list.get(1)).isEqualTo(WordPair.of("c", "d"));
    }

    @Test
    public void testAddAllWithDuplicateElements() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        list.addAll(Set.of(10, 20, 30));
        boolean result = list.addAll(List.of(20, 20, 30, 30, 40, 50));
        assertThat(result).isTrue();
        assertThat(list).hasSize(5);
        assertThat(list.get(0)).isEqualTo(10);
        assertThat(list.get(1)).isEqualTo(20);
        assertThat(list.get(2)).isEqualTo(30);
        assertThat(list.get(3)).isEqualTo(40);
        assertThat(list.get(4)).isEqualTo(50);
    }

    @Test
    public void testAddWithIndexAndElement() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> list.add(0, 1));
        assertThat(exception).hasMessage("You cannot directly add an item to an index");
    }

    @Test
    public void testAddAllWithIndexAndCollection() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, Set.of()));
        assertThat(exception).hasMessage("You cannot directly set add items to an index");
    }

    @Test
    public void testSetWithIndexAndElement() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> list.set(0, 0));
        assertThat(exception).hasMessage("You cannot directly set an item");
    }

    @Test
    public void testEquals() {
        List<Integer> list1 = new UniqueSortedList<>(comparingInt(x -> x));
        List<Integer> list2 = new UniqueSortedList<>(comparingInt(x -> x));
        boolean result = list1.equals(list2);
        assertTrue(result);
    }

    @Test
    public void testHashCode() {
        List<Integer> list = new UniqueSortedList<>(comparingInt(x -> x));
        int result = list.hashCode();
        assertThat(result).isEqualTo(1);
    }

}
