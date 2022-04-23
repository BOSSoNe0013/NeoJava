package com.b1project.udooneo.messages.response;

import java.util.List;

import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.net.NeoJavaProtocol;

/**
 *  Copyright (C) 2015 Cyril BOSSELUT <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava Tools for UDOO Neo
 *
 *  NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This libraries are distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <<a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>>.
 *
 */

@SuppressWarnings("unused")
public class ResponseExportGpios extends ResponseMessage {

	private List<Pin> content;
	
	public ResponseExportGpios(String info, List<Pin> gpios) {
		super(NeoJavaProtocol.RESP_GPIOS_EXPORT,info);
		this.content = gpios;
	}

	public List<Pin> getContent() {
		return content;
	}

	public void setContent(List<Pin> content) {
		this.content = content;
	}
}
