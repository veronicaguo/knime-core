/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 */
package org.knime.core.jaxrs.workflow.entity;

import org.knime.core.gateway.v0.workflow.entity.StyleRangeEnt;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link StyleRangeEnt} interface that can be deserialized from a json object (json-annotated constructor).
 *
 * @author Martin Horn, University of Konstanz
 */
// AUTO-GENERATED CODE; DO NOT MODIFY
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME, 
  include = JsonTypeInfo.As.PROPERTY, 
  property = "EntityType")
@JsonSubTypes({ 
  @Type(value = StyleRangeEntFromJson.class, name = "StyleRangeEnt")
})
public class StyleRangeEntFromJson  implements StyleRangeEnt {

	private int m_Start;
	private int m_Length;
	private String m_FontName;
	private String m_FontStyle;
	private int m_FontSize;
	private int m_ForegroundColor;

	@JsonCreator
	private StyleRangeEntFromJson(
	@JsonProperty("Start") int Start,	@JsonProperty("Length") int Length,	@JsonProperty("FontName") String FontName,	@JsonProperty("FontStyle") String FontStyle,	@JsonProperty("FontSize") int FontSize,	@JsonProperty("ForegroundColor") int ForegroundColor	) {
		m_Start = Start;
		m_Length = Length;
		m_FontName = FontName;
		m_FontStyle = FontStyle;
		m_FontSize = FontSize;
		m_ForegroundColor = ForegroundColor;
	}
	
	protected StyleRangeEntFromJson() {
		//just a dummy constructor for subclasses
	}


	@Override
    public int getStart() {
        	return m_Start;
            
    }
    
	@Override
    public int getLength() {
        	return m_Length;
            
    }
    
	@Override
    public String getFontName() {
        	return m_FontName;
            
    }
    
	@Override
    public String getFontStyle() {
        	return m_FontStyle;
            
    }
    
	@Override
    public int getFontSize() {
        	return m_FontSize;
            
    }
    
	@Override
    public int getForegroundColor() {
        	return m_ForegroundColor;
            
    }
    

}