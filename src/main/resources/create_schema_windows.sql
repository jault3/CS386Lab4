SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS lab4 ;
CREATE SCHEMA IF NOT EXISTS lab4 DEFAULT CHARACTER SET latin1 ;
USE lab4 ;

-- -----------------------------------------------------
-- Table lab4.owner
-- -----------------------------------------------------
DROP TABLE IF EXISTS lab4.owner ;

CREATE TABLE IF NOT EXISTS lab4.owner (
  id INT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(255) NULL,
  phone_number VARCHAR(12) NULL,
  last_name VARCHAR(255) NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table lab4.unit
-- -----------------------------------------------------
DROP TABLE IF EXISTS lab4.unit ;

CREATE TABLE IF NOT EXISTS lab4.unit (
  name VARCHAR(255) NOT NULL,
  number INT NOT NULL,
  minimum INT NULL,
  cost INT NULL,
  PRIMARY KEY (name, number))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table lab4.owner_has_unit
-- -----------------------------------------------------
DROP TABLE IF EXISTS lab4.owner_has_unit ;

CREATE TABLE IF NOT EXISTS lab4.owner_has_unit (
  owner_id INT NOT NULL,
  unit_name VARCHAR(255) NOT NULL,
  unit_number INT NOT NULL,
  week_number INT NOT NULL,
  PRIMARY KEY (owner_id, unit_name, unit_number, week_number),
  INDEX fk_owner_has_unit_unit1_idx (unit_name ASC, unit_number ASC),
  INDEX fk_owner_has_unit_owner_idx (owner_id ASC),
  CONSTRAINT fk_owner_has_unit_owner
    FOREIGN KEY (owner_id)
    REFERENCES lab4.owner (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_owner_has_unit_unit1
    FOREIGN KEY (unit_name , unit_number)
    REFERENCES lab4.unit (name , number)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Load data from text files
-- -----------------------------------------------------

Load Data local Infile 'owner_data.txt' into table lab4.owner fields terminated by '\t' optionally enclosed by '"' lines terminated by '\r\n' ignore 1 lines ;
Load Data local Infile 'unit_data.txt' into table lab4.unit fields terminated by '\t' optionally enclosed by '"' lines terminated by '\r\n' ignore 1 lines ;
Load Data local Infile 'owner_has_unit_data.txt' into table lab4.owner_has_unit fields terminated by '\t' optionally enclosed by '"' lines terminated by '\r\n' ignore 1 lines ;