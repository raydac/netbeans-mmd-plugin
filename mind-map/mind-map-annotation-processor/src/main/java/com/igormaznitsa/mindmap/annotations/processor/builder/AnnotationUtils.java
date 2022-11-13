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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.tuple.Pair;

public final class AnnotationUtils {

  private AnnotationUtils() {
  }

  public static UriLine findPosition(
      final SourcePositions sourcePositions, final Trees trees, final Element element) {
    final TreePath treePath = trees.getPath(element);
    final CompilationUnitTree compilationUnit = treePath.getCompilationUnit();

    final long startPosition =
        sourcePositions.getStartPosition(compilationUnit, treePath.getLeaf());
    final long lineNumber = compilationUnit.getLineMap().getLineNumber(startPosition);
    return new UriLine(compilationUnit.getSourceFile().toUri(), lineNumber);
  }

  public static <A extends Annotation> List<Pair<A, Element>> findAmongEnclosingAndAncestors(
      final Element element, final Class<A> annotationType, final Types typeUtils) {
    final List<Pair<A, Element>> result = new ArrayList<>();
    result.addAll(findFirstWithEnclosing(element, annotationType, false));
    result.addAll(findFirstWithAncestors(element, annotationType, typeUtils, false));
    return result;
  }

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

  public static <A extends Annotation> List<Pair<A, Element>> findFirstWithAncestors(
      final Element element,
      final Class<A> annotationType,
      final Types typeUtils,
      final boolean includeElementInSearch) {
    if (includeElementInSearch) {
      final A[] found = element.getAnnotationsByType(annotationType);
      if (found.length > 0) {
        return Stream.of(found)
            .map(x -> Pair.of(x, element))
            .collect(Collectors.toList());
      }
    }

    try {
      final List<Pair<A, Element>> found = new ArrayList<>();
      final List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(element.asType());

      supertypes.forEach(
          x -> {
            final Element e = typeUtils.asElement(x);
            found.addAll(
                Stream.of(e.getAnnotationsByType(annotationType))
                    .map(annotation -> Pair.of(annotation, e))
                    .collect(Collectors.toList()));
          });

      if (found.isEmpty()) {
        for (final TypeMirror m : supertypes) {
          found.addAll(
              findFirstWithAncestors(typeUtils.asElement(m), annotationType, typeUtils, true));
          if (!found.isEmpty()) {
            break;
          }
        }
      }
      return found;
    } catch (IllegalArgumentException ex) {
      return List.of();
    }
  }

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
