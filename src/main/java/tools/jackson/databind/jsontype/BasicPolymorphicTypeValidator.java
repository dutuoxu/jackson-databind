package tools.jackson.databind.jsontype;

import java.util.*;
import java.util.regex.Pattern;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;

/**
 * Standard {@link BasicPolymorphicTypeValidator} implementation that users may want
 * to use for constructing validators based on simple class hierarchy and/or name patterns
 * to allow and/or deny certain subtypes.
 *<p>
 * Most commonly this is used to allow known safe subtypes based on common super type
 * or Java package name.
 *<br>
 * For example:
 *<pre>
 *</pre>
 */
public class BasicPolymorphicTypeValidator
    extends PolymorphicTypeValidator.Base
    implements java.io.Serializable
{
    private static final long serialVersionUID = 3L;

    /*
    /**********************************************************************
    /* Helper types: matchers
    /**********************************************************************
     */

    /**
     * General matcher interface (predicate) for validating class values
     * (base type or resolved subtype)
     */
    @FunctionalInterface
    public interface TypeMatcher {
        public abstract boolean match(DatabindContext ctxt, Class<?> clazz);
    }

    /**
     * General matcher interface (predicate) for validating unresolved
     * subclass class name.
     */
    @FunctionalInterface
    public interface NameMatcher {
        public abstract boolean match(DatabindContext ctxt, String clazzName);
    }

    /*
    /**********************************************************************
    /* Builder class for configuring instances
    /**********************************************************************
     */

    /**
     * Builder class for configuring and constructing immutable
     * {@link BasicPolymorphicTypeValidator} instances. Criteria for allowing
     * polymorphic subtypes is specified by adding rules in priority order, starting
     * with the rules to evaluate first: when a matching rule is found, its status
     * ({@link PolymorphicTypeValidator.Validity#ALLOWED} or {@link PolymorphicTypeValidator.Validity#DENIED}) is used and no further
     * rules are checked.
     */
    public static class Builder {
        /**
         * Optional set of base types (exact match) that are NOT accepted
         * as base types for polymorphic properties. May be used to prevent "unsafe"
         * base types like {@link java.lang.Object} or {@link java.io.Serializable}.
         */
        protected Set<Class<?>> _invalidBaseTypes;

        /**
         * Collected matchers for base types to allow.
         */
        protected List<TypeMatcher> _baseTypeMatchers;

        /**
         * Collected name-based matchers for sub types to allow.
         */
        protected List<NameMatcher> _subTypeNameMatchers;

        /**
         * Collected Class-based matchers for sub types to allow.
         */
        protected List<TypeMatcher> _subTypeClassMatchers;

        protected Builder() { }

        // // Methods for checking solely by base type (before subtype even considered)

        /**
         * Method for appending matcher that will allow all subtypes in cases where
         * nominal base type is specified class, or one of its subtypes.
         * For example, call to
         *<pre>
         *    builder.allowIfBaseType(MyBaseType.class)
         *</pre>
         * would indicate that any polymorphic properties where declared base type
         * is {@code MyBaseType} (or subclass thereof) would allow all legal (assignment-compatible)
         * subtypes.
         */
        public Builder allowIfBaseType(final Class<?> baseOfBase) {
            return _appendBaseMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> clazz) {
                    return baseOfBase.isAssignableFrom(clazz);
                }
            });
        }

        /**
         * Method for appending matcher that will allow all subtypes in cases where
         * nominal base type's class name matches given {@link Pattern}
         * For example, call to
         *<pre>
         *    builder.allowIfBaseType(Pattern.compile("com\\.mycompany\\..*")
         *</pre>
         * would indicate that any polymorphic properties where declared base type
         * is in package {@code com.mycompany} would allow all legal (assignment-compatible)
         * subtypes.
         *<p>
         * NOTE! {@link Pattern} match is applied using
         *<code>
         *   if (patternForBase.matcher(typeId).matches()) { }
         *</code>
         * that is, it must match the whole class name, not just part.
         */
        public Builder allowIfBaseType(final Pattern patternForBase) {
            return _appendBaseMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> clazz) {
                    return patternForBase.matcher(clazz.getName()).matches();
                }
            });
        }

        /**
         * Method for appending matcher that will allow all subtypes in cases where
         * nominal base type's class name starts with specific prefix.
         * For example, call to
         *<pre>
         *    builder.allowIfBaseType("com.mycompany.")
         *</pre>
         * would indicate that any polymorphic properties where declared base type
         * is in package {@code com.mycompany} would allow all legal (assignment-compatible)
         * subtypes.
         */
        public Builder allowIfBaseType(final String prefixForBase) {
            return _appendBaseMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> clazz) {
                    return clazz.getName().startsWith(prefixForBase);
                }
            });
        }

        /**
         * Method for appending custom matcher called with base type: if matcher returns
         * {@code true}, all possible subtypes will be accepted; if {@code false}, other
         * matchers are applied.
         *
         * @param matcher Custom matcher to apply to base type
         *
         * @return This Builder to allow call chaining
         */
        public Builder allowIfBaseType(final TypeMatcher matcher) {
            return _appendBaseMatcher(matcher);
        }

        /**
         * Method for appending matcher that will mark any polymorphic properties with exact
         * specific class to be invalid.
         * For example, call to
         *<pre>
         *    builder.denyforExactBaseType(Object.class)
         *</pre>
         * would indicate that any polymorphic properties where declared base type
         * is {@code java.lang.Object}
         * would be deemed invalid, and attempt to deserialize values of such types
         * should result in an exception.
         */
        public Builder denyForExactBaseType(final Class<?> baseTypeToDeny) {
            if (_invalidBaseTypes == null) {
                _invalidBaseTypes = new HashSet<>();
            }
            _invalidBaseTypes.add(baseTypeToDeny);
            return this;
        }

        // // Methods for considering subtype (base type was not enough)

        /**
         * Method for appending matcher that will allow specific subtype (regardless
         * of declared base type) if it is {@code subTypeBase} or its subtype.
         * For example, call to
         *<pre>
         *    builder.allowIfSubType(MyImplType.class)
         *</pre>
         * would indicate that any polymorphic values with type of
         * is {@code MyImplType} (or subclass thereof)
         * would be allowed.
         */
        public Builder allowIfSubType(final Class<?> subTypeBase) {
            return _appendSubClassMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> clazz) {
                    return subTypeBase.isAssignableFrom(clazz);
                }
            });
        }

        /**
         * Method for appending matcher that will allow specific subtype (regardless
         * of declared base type) in cases where subclass name matches given {@link Pattern}.
         * For example, call to
         *<pre>
         *    builder.allowIfSubType(Pattern.compile("com\\.mycompany\\.")
         *</pre>
         * would indicate that any polymorphic values in package {@code com.mycompany}
         * would be allowed.
         *<p>
         * NOTE! {@link Pattern} match is applied using
         *<code>
         *   if (patternForSubType.matcher(typeId).matches()) { }
         *</code>
         * that is, it must match the whole class name, not just part.
         */
        public Builder allowIfSubType(final Pattern patternForSubType) {
            return _appendSubNameMatcher(new NameMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, String clazzName) {
                    return patternForSubType.matcher(clazzName).matches();
                }
            });
        }

        /**
         * Method for appending matcher that will allow specific subtype (regardless
         * of declared base type)
         * in cases where subclass name starts with specified prefix
         * For example, call to
         *<pre>
         *    builder.allowIfSubType("com.mycompany.")
         *</pre>
         * would indicate that any polymorphic values in package {@code com.mycompany}
         * would be allowed.
         */
        public Builder allowIfSubType(final String prefixForSubType) {
            return _appendSubNameMatcher(new NameMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, String clazzName) {
                    return clazzName.startsWith(prefixForSubType);
                }
            });
        }

        /**
         * Method for appending custom matcher called with resolved subtype: if matcher returns
         * {@code true}, type will be accepted; if {@code false}, other
         * matchers are applied.
         *
         * @param matcher Custom matcher to apply to resolved subtype
         *
         * @return This Builder to allow call chaining
         */
        public Builder allowIfSubType(final TypeMatcher matcher) {
            return _appendSubClassMatcher(matcher);
        }

        /**
         * Method for appending matcher that will allow all subtypes that are Java arrays
         * (regardless of element type). Note that this does NOT validate element type
         * itself as long as Polymorphic Type handling is enabled for element type: this
         * is the case with all standard "Default Typing" inclusion criteria as well as for
         * annotation ({@code @JsonTypeInfo}) use case (since annotation only applies to element
         * types, not container).
         *<p>
         * NOTE: not used with other Java collection types ({@link java.util.List}s,
         *    {@link java.util.Collection}s), mostly since use of generic types as polymorphic
         *    values is not (well) supported.
         */
        public Builder allowIfSubTypeIsArray() {
            return _appendSubClassMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> clazz) {
                    return clazz.isArray();
                }
            });
        }

        /**
         * Method for appending matcher that will allow all subtypes for which a
         * {@link tools.jackson.databind.ValueDeserializer})
         * is explicitly provided by either {@code jackson-databind} itself or one of registered
         * {@link tools.jackson.databind.JacksonModule}s.
         * Determination is implementation by calling
         * {@link tools.jackson.databind.deser.DeserializerFactory#hasExplicitDeserializerFor}.
         *<p>
         * In practice this matcher should remove the need to register any standard Jackson-supported
         * JDK types, as well as most if not all 3rd party types; leaving only POJOs and those 3rd party
         * types that are not supported by relevant modules. In turn this should not open security
         * holes to "gadget" types since insecure types should not be supported by datatype modules.
         * For highest security cases (where input is untrusted) it is still preferable to add
         * more specific allow-rules, if possible.
         *<p>
         * NOTE: Modules need to provide support for detection so if 3rd party types do not seem to
         * be supported, Module in question may need to be updated to indicate existence of explicit
         * deserializers.
         */
        public Builder allowSubTypesWithExplicitDeserializer() {
            return _appendSubClassMatcher(new TypeMatcher() {
                @Override
                public boolean match(DatabindContext ctxt, Class<?> valueType) {
                    return ((DeserializationContext) ctxt).hasExplicitDeserializerFor(valueType);
                }
            });
        }

        public BasicPolymorphicTypeValidator build() {
            return new BasicPolymorphicTypeValidator(_invalidBaseTypes,
                    (_baseTypeMatchers == null) ? null : _baseTypeMatchers.toArray(new TypeMatcher[0]),
                    (_subTypeNameMatchers == null) ? null : _subTypeNameMatchers.toArray(new NameMatcher[0]),
                    (_subTypeClassMatchers == null) ? null : _subTypeClassMatchers.toArray(new TypeMatcher[0])
            );
        }

        protected Builder _appendBaseMatcher(TypeMatcher matcher) {
            if (_baseTypeMatchers == null) {
                _baseTypeMatchers = new ArrayList<>();
            }
            _baseTypeMatchers.add(matcher);
            return this;
        }

        protected Builder _appendSubNameMatcher(NameMatcher matcher) {
            if (_subTypeNameMatchers == null) {
                _subTypeNameMatchers = new ArrayList<>();
            }
            _subTypeNameMatchers.add(matcher);
            return this;
        }

        protected Builder _appendSubClassMatcher(TypeMatcher matcher) {
            if (_subTypeClassMatchers == null) {
                _subTypeClassMatchers = new ArrayList<>();
            }
            _subTypeClassMatchers.add(matcher);
            return this;
        }
    }

    /*
    /**********************************************************************
    /* Actual implementation
    /**********************************************************************
     */

    /**
     * Set of specifically denied base types to indicate that use of specific
     * base types is not allowed: most commonly used to fully block use of
     * {@link java.lang.Object} as the base type.
     */
    protected final Set<Class<?>> _invalidBaseTypes;

    /**
     * Set of matchers that can validate all values of polymorphic properties
     * that match specified allowed base types.
     */
    protected final TypeMatcher[] _baseTypeMatchers;

    /**
     * Set of matchers that can validate specific values of polymorphic properties
     * that match subtype class name criteria.
     */
    protected final NameMatcher[] _subTypeNameMatchers;

    /**
     * Set of matchers that can validate specific values of polymorphic properties
     * that match subtype class criteria.
     */
    protected final TypeMatcher[] _subClassMatchers;

    protected BasicPolymorphicTypeValidator(Set<Class<?>> invalidBaseTypes,
            TypeMatcher[] baseTypeMatchers,
            NameMatcher[] subTypeNameMatchers, TypeMatcher[] subClassMatchers) {
        _invalidBaseTypes = invalidBaseTypes;
        _baseTypeMatchers = baseTypeMatchers;
        _subTypeNameMatchers = subTypeNameMatchers;
        _subClassMatchers = subClassMatchers;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Validity validateBaseType(DatabindContext ctxt, JavaType baseType)
    {
        final Class<?> rawBase = baseType.getRawClass();
        if (_invalidBaseTypes != null) {
            if (_invalidBaseTypes.contains(rawBase)) {
                return Validity.DENIED;
            }
        }
        if (_baseTypeMatchers != null) {
            for (TypeMatcher m : _baseTypeMatchers) {
                if (m.match(ctxt, rawBase)) {
                    return Validity.ALLOWED;
                }
            }
        }
        return Validity.INDETERMINATE;
    }

    @Override
    public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType,
            String subClassName)
    {
//System.err.println("validateSubClassName('"+subClassName+"')");
        if (_subTypeNameMatchers != null)  {
            for (NameMatcher m : _subTypeNameMatchers) {
                if (m.match(ctxt, subClassName)) {
                    return Validity.ALLOWED;
                }
            }
        }
        // could not yet decide, so:
        return Validity.INDETERMINATE;
    }

    @Override
    public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType)
    {
        if (_subClassMatchers != null)  {
            final Class<?> subClass = subType.getRawClass();
            for (TypeMatcher m : _subClassMatchers) {
                if (m.match(ctxt, subClass)) {
                    return Validity.ALLOWED;
                }
            }
        }
        // could not decide, callers gets to decide; usually will deny
        return Validity.INDETERMINATE;
    }
}
