# Issue #262: Implement other field components

## Context
Building on the base field component architecture from issue #261, we need to implement the remaining specialized field components to complete the task template field system. These components will provide users with a comprehensive set of input types for creating flexible task templates.

## Objectives

### 1. NumberFieldComponent
Implement a numeric input field with the following features:
- Numeric value input with proper validation
- Min/max constraint support
- Decimal/integer support
- Optional unit display (e.g., "kg", "meters", "%")
- String-to-number conversion with proper formatting

### 2. DateFieldComponent
Create a date selection component:
- Integration with Material3 DatePicker
- Optional time support
- Date range constraints (min/max dates)
- Epoch millisecond conversion for internal storage
- Human-readable date display formatting

### 3. SingleSelectFieldComponent
Build a dropdown selection component:
- Human-readable label display with value mapping
- Integration with Material3 ExposedDropdownMenu
- Custom value input support
- State synchronization when switching to edit mode (using LaunchedEffect)
- Proper handling of initial values and mode transitions

### 4. MultiSelectFieldComponent
Implement a multi-selection component:
- Visual selection using Material3 FilterChips
- Immediate visual feedback without explicit save actions
- Min/max selection constraints
- Custom value support
- Proper state management for chip selection

### 5. Comprehensive Testing
Each component must have complete test coverage:
- NumberFieldComponentTest
- DateFieldComponentTest
- SingleSelectFieldComponentTest
- MultiSelectFieldComponentTest
- All tests validate interaction modes, validation, constraints, and edge cases

## Technical Requirements
- All components must extend the BaseFieldComponent architecture
- Use the generic `renderer` lambda pattern for field-specific UI
- Inherit common functionality (labels, validation, mode switching) from base component
- Handle complex state synchronization properly
- Type-safe implementations with proper value conversions

## Acceptance Criteria
- [ ] NumberFieldComponent is fully functional with numeric validation and unit display
- [ ] DateFieldComponent integrates Material3 DatePicker with proper date handling
- [ ] SingleSelectFieldComponent provides dropdown selection with state synchronization
- [ ] MultiSelectFieldComponent supports multi-selection with FilterChips
- [ ] All four components work correctly in all three interaction modes (EditOnly, ViewOnly, Toggleable)
- [ ] Comprehensive test suites validate all functionality and edge cases
- [ ] All tests pass
- [ ] Components are reusable for forms beyond task templates

## Known Considerations
- **MultiSelectFieldComponent state synchronization**: Rapid value changes during mode transitions might cause temporary state inconsistencies due to LaunchedEffect timing
- **SingleSelectFieldComponent**: LaunchedEffect is used to synchronize state when switching to edit mode to prevent stale displayed values

## Future Improvements
These field components are designed to be reusable and could be leveraged for all forms throughout the application beyond task templates.

---

**Related:** Closes via PR #265 | **Depends on:** #261
