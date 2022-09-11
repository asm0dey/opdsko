/*
 * opdsko
 * Copyright (C) 2022  asm0dey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is generated by jOOQ.
 */
package io.github.asm0dey.opdsko.jooq.tables.interfaces;


import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IAuthor extends Serializable {

    /**
     * Getter for <code>author.id</code>.
     */
    public Long getId();

    /**
     * Getter for <code>author.fb2id</code>.
     */
    public String getFb2id();

    /**
     * Getter for <code>author.first_name</code>.
     */
    public String getFirstName();

    /**
     * Getter for <code>author.middle_name</code>.
     */
    public String getMiddleName();

    /**
     * Getter for <code>author.last_name</code>.
     */
    public String getLastName();

    /**
     * Getter for <code>author.nickname</code>.
     */
    public String getNickname();

    /**
     * Getter for <code>author.added</code>.
     */
    public LocalDateTime getAdded();
}
