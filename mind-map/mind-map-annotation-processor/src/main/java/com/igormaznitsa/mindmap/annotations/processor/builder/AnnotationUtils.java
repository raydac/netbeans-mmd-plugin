package com.igormaznitsa.mindmap.annotations.processor.builder;

import static java.util.Objects.requireNonNull;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Auxiliary class contains annotation processing utility methods.
 */
public final class AnnotationUtils {

  private AnnotationUtils() {
  }

  /**
   * Find source line position for element.
   *
   * @param sourcePositions auxiliary utility class, must not be null
   * @param trees           auxiliary utility class, must not be null
   * @param element         element which position should be found
   * @return formed container with URI and line number.
   */
  public static UriLine findPosition(
      final SourcePositions sourcePositions, final Trees trees, final Element element) {
    final TreePath treePath = trees.getPath(element);
    final CompilationUnitTree compilationUnit = treePath.getCompilationUnit();

    final long startPosition =
        sourcePositions.getStartPosition(compilationUnit, treePath.getLeaf());
    final long lineNumber = compilationUnit.getLineMap().getLineNumber(startPosition);
    return new UriLine(compilationUnit.getSourceFile().toUri(), lineNumber);
  }

  /**
   * Find first annotations for element, start of list contain annotations found among enclosing elements, tails contains annotations found among ancestors.
   *
   * @param element        element to find annotations, can be null
   * @param annotationType annotation type, must not be null
   * @param typeUtils      type utils class, must not be null
   * @param <A>            annotation type
   * @return list of annotations and elements found by request, must not be null
   */
  public static <A extends Annotation> List<Pair<A, Element>> findFirstAmongEnclosingAndAncestors(
      final Element element,
      final Class<A> annotationType,
      final Types typeUtils
  ) {
    final List<Pair<A, Element>> result = new ArrayList<>();
    result.addAll(findFirstWithEnclosing(element, annotationType, false));
    result.addAll(findFirstWithAncestors(element, annotationType, typeUtils, false));
    return result;
  }

  /**
   * Find all class or interface elements.
   *
   * @param element element which enclosing elements to find, can be null
   * @return found class and interface elements for element, can't be null
   */
  public static List<Element> findAllTypeElements(final Element element) {
    if (element == null) {
      return List.of();
    }

    List<Element> result = new ArrayList<>();
    if (element.getKind().isInterface() || element.getKind().isClass()) {
      result.add(element);
    }
    result.addAll(findAllTypeElements(element.getEnclosingElement()));
    return result;
  }

  /**
   * Find enclosing type for element (i.e. class or interface)
   *
   * @param element target element, must not be null
   * @return found type element or empty otherwise
   */
  public static Optional<? extends Element> findEnclosingType(final Element element) {
    if (element == null) {
      return Optional.empty();
    }
    if (element.getKind() == ElementKind.MODULE || element.getKind() == ElementKind.PACKAGE) {
      return element.getEnclosedElements().stream()
          .filter(e -> e.getKind().isClass() || e.getKind().isInterface())
          .findFirst();
    } else if (element.getKind().isClass() || element.getKind().isInterface()) {
      return Optional.of(element);
    } else {
      return findEnclosingType(element.getEnclosingElement());
    }
  }

  /**
   * Find first required annotations among enclosing elements.
   *
   * @param element        target element, can be null
   * @param annotationType annotation to find, must not be null
   * @param includeElement flag to include target element into search
   * @param <A>            annotation type
   * @return list of pairs found annotations, must not be null
   */
  public static <A extends Annotation> List<Pair<A, Element>> findFirstWithEnclosing(
      final Element element, final Class<A> annotationType, final boolean includeElement) {
    if (element == null) {
      return List.of();
    }
    final List<A> found =
        includeElement ? Arrays.asList(element.getAnnotationsByType(annotationType)) : List.of();
    if (found.isEmpty()) {
      return findFirstWithEnclosing(element.getEnclosingElement(), annotationType, true);
    } else {
      return found.stream().map(x -> Pair.of(x, element)).collect(Collectors.toList());
    }
  }

  /**
   * Find first required annotations among ancestors.
   *
   * @param element        target element, can be null
   * @param annotationType annotation to find, must not be null
   * @param includeElement flag to include target element into search
   * @param <A>            annotation type
   * @return list of pairs found annotations, must not be null
   */
  public static <A extends Annotation> List<Pair<A, Element>> findFirstWithAncestors(
      final Element element,
      final Class<A> annotationType,
      final Types typeUtils,
      final boolean includeElement) {
    if (element == null) {
      return List.of();
    }
    if (includeElement) {
      final A[] found = element.getAnnotationsByType(annotationType);
      if (found.length > 0) {
        return Stream.of(found)
            .map(x -> Pair.of(x, element))
            .collect(Collectors.toList());
      }
    }

    final List<Element> superElements = findAllTypeElements(element)
        .stream()
        .flatMap(x -> {
          try {
            return typeUtils.directSupertypes(x.asType()).stream();
          } catch (IllegalArgumentException ex) {
            return Stream.empty();
          }
        })
        .map(typeUtils::asElement)
        .collect(Collectors.toList());

    return superElements.stream()
        .map(x -> findFirstWithAncestors(x, annotationType, typeUtils, true))
        .filter(x -> !x.isEmpty())
        .findFirst()
        .orElse(List.of());
  }

  /**
   * Auxiliary container class to keep information about line and sources.
   */
  public static final class UriLine {
    private final URI uri;
    private final long line;

    private UriLine(final URI uri, final long line) {
      this.uri = requireNonNull(uri);
      this.line = line;
    }

    public URI getUri() {
      return this.uri;
    }

    public long getLine() {
      return this.line;
    }
  }
}
