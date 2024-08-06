package com.dox.ara.command

import com.dox.ara.command.types.AlarmCommandHandler
import com.dox.ara.command.types.BookCabCommandHandler
import com.dox.ara.command.types.CallCommandHandler
import com.dox.ara.command.types.IncomingCallCommandHandler
import com.dox.ara.command.types.MusicControlCommandHandler
import com.dox.ara.command.types.PayQrCommandHandler
import com.dox.ara.command.types.PayUpiCommandHandler
import com.dox.ara.command.types.PlayMusicCommandHandler
import com.dox.ara.command.types.SettingCommandHandler
import com.dox.ara.command.types.VolumeCommandHandler
import dagger.assisted.AssistedFactory

@AssistedFactory
interface SettingCommandHandlerFactory {
    fun create(args: List<String>): SettingCommandHandler
}

@AssistedFactory
interface PayUpiCommandHandlerFactory {
    fun create(args: List<String>): PayUpiCommandHandler
}

@AssistedFactory
interface PayQrCommandHandlerFactory {
    fun create(args: List<String>): PayQrCommandHandler
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
interface CallCommandHandlerFactory {
    fun create(args: List<String>): CallCommandHandler
}

@AssistedFactory
interface AlarmCommandHandlerFactory {
    fun create(args: List<String>): AlarmCommandHandler
}

@AssistedFactory
interface PlayMusicCommandHandlerFactory {
    fun create(args: List<String>): PlayMusicCommandHandler
}

@AssistedFactory
interface MusicControlCommandHandlerFactory {
    fun create(args: List<String>): MusicControlCommandHandler
}

@AssistedFactory
interface BookCabCommandHandlerFactory {
    fun create(args: List<String>): BookCabCommandHandler
}