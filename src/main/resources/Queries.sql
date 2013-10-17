# Part 2
SELECT last_name, first_name, phone_number FROM owner ORDER BY last_name, first_name;

# Part 3
SELECT last_name, first_name FROM 'owner', 'owner_has_unit', 'unit' WHERE unit.name = ? AND
                                                                              unit.number = ?
GROUP BY last_name, first_name HAVING COUNT(DISTINCT week_number) >= ?;

# Part 4 - Not Done
SELECT last_name, first_name, other FROM 'owner', 'owner_has_unit', 'unit' WHERE unit.name = ? AND
                                                                                 unit.number = ?
  ORDER BY last_name, first_name;