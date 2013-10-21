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
  
# Part 5
select o.last_name, o.first_name, count(*) weeks_owned
from owner o, owner_has_unit u
where o.id = u.owner_id
     and u.unit_name = '?????'
group by o.last_name having weeks_owned > 1
order by o.last_name, o.first_name;

# Part 6
select ohu.unit_name,ohu.unit_number, ohu.week_number 
from owner o, owner_has_unit ohu
where o.last_name = '?????'
     and o.id = ohu.owner_id
order by ohu.unit_name, ohu.unit_number, ohu.week_number;

#PART 7
select o.last_name, o.first_name, ohu.week_number
from owner o, owner_has_unit ohu
where ohu.owner_id = o.id
     and ohu.unit_name = '?????'
     and ohu.unit_number = '?????'
order by ohu.week_number;

#PART 8
select o.last_name, o.first_name, ohu.unit_name, ohu.unit_number
from owner o, owner_has_unit ohu
where ohu.owner_id = o.id
     and ohu.week_number = '?????'
order by ohu.unit_name, ohu.unit_number, o.last_name;
