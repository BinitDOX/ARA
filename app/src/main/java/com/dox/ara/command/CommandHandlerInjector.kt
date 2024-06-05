package com.dox.ara.command

import com.dox.ara.command.types.AlarmCommandHandler
import com.dox.ara.command.types.IncomingCallCommandHandler
import com.dox.ara.command.types.PayCommandHandler
import com.dox.ara.command.types.SettingCommandHandler
import com.dox.ara.command.types.VolumeCommandHandler
import dagger.assisted.AssistedFactory

@AssistedFactory
interface SettingCommandHandlerFactory {
    fun create(args: List<String>): SettingCommandHandler
}

@AssistedFactory
interface PayCommandHandlerFactory {
    fun create(args: List<String>): PayCommandHandler
}

@AssistedFactory
interface VolumeCommandHandlerFactory {
    fun create(args: List<String>): VolumeCommandHandler
}

@AssistedFactory
interface IncomingCallCommandHandlerFactory {
    fun create(args: List<String>): IncomingCallCommandHandler
}

@AssistedFactory
interface AlarmCommandHandlerFactory {
    fun create(args: List<String>): AlarmCommandHandler
}