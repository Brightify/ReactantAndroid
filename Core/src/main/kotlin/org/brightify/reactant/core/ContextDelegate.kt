package org.brightify.reactant.core

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.support.annotation.RequiresApi
import android.view.Display
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ContextDelegate(internal var delegate: Context): Context() {

    override fun getApplicationContext(): Context {
        return delegate.applicationContext
    }

    override fun setWallpaper(bitmap: Bitmap?) {
        delegate.setWallpaper(bitmap)
    }

    override fun setWallpaper(data: InputStream?) {
        delegate.setWallpaper(data)
    }

    override fun removeStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        delegate.removeStickyBroadcastAsUser(intent, user)
    }

    override fun checkCallingOrSelfPermission(permission: String?): Int {
        return delegate.checkCallingOrSelfPermission(permission)
    }

    override fun getClassLoader(): ClassLoader {
        return delegate.classLoader
    }

    override fun checkCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int): Int {
        return delegate.checkCallingOrSelfUriPermission(uri, modeFlags)
    }

    override fun getObbDir(): File {
        return delegate.obbDir
    }

    override fun checkUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int): Int {
        return delegate.checkUriPermission(uri, pid, uid, modeFlags)
    }

    override fun checkUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int): Int {
        return delegate.checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags)
    }

    override fun getExternalFilesDirs(type: String?): Array<File> {
        return delegate.getExternalFilesDirs(type)
    }

    override fun getPackageResourcePath(): String {
        return delegate.packageResourcePath
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun deleteSharedPreferences(name: String?): Boolean {
        return delegate.deleteSharedPreferences(name)
    }

    override fun checkPermission(permission: String?, pid: Int, uid: Int): Int {
        return delegate.checkPermission(permission, pid, uid)
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) {
        delegate.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags)
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int,
                                   options: Bundle?) {
        delegate.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options)
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return delegate.getSharedPreferences(name, mode)
    }

    override fun sendStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        delegate.sendStickyBroadcastAsUser(intent, user)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getDataDir(): File {
        return delegate.dataDir
    }

    override fun getWallpaper(): Drawable {
        return delegate.wallpaper
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun isDeviceProtectedStorage(): Boolean {
        return delegate.isDeviceProtectedStorage
    }

    override fun getExternalFilesDir(type: String?): File {
        return delegate.getExternalFilesDir(type)
    }

    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        delegate.sendBroadcastAsUser(intent, user)
    }

    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?) {
        delegate.sendBroadcastAsUser(intent, user, receiverPermission)
    }

    override fun getExternalCacheDir(): File {
        return delegate.externalCacheDir
    }

    override fun getDatabasePath(name: String?): File {
        return delegate.getDatabasePath(name)
    }

    override fun getFileStreamPath(name: String?): File {
        return delegate.getFileStreamPath(name)
    }

    override fun stopService(service: Intent?): Boolean {
        return delegate.stopService(service)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun checkSelfPermission(permission: String?): Int {
        return delegate.checkSelfPermission(permission)
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent {
        return delegate.registerReceiver(receiver, filter)
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, broadcastPermission: String?,
                                  scheduler: Handler?): Intent {
        return delegate.registerReceiver(receiver, filter, broadcastPermission, scheduler)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getSystemServiceName(serviceClass: Class<*>?): String {
        return delegate.getSystemServiceName(serviceClass)
    }

    override fun getMainLooper(): Looper {
        return delegate.mainLooper
    }

    override fun enforceCallingOrSelfPermission(permission: String?, message: String?) {
        delegate.enforceCallingOrSelfPermission(permission, message)
    }

    override fun getPackageCodePath(): String {
        return delegate.packageCodePath
    }

    override fun checkCallingUriPermission(uri: Uri?, modeFlags: Int): Int {
        return delegate.checkCallingUriPermission(uri, modeFlags)
    }

    override fun getWallpaperDesiredMinimumWidth(): Int {
        return delegate.wallpaperDesiredMinimumWidth
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun createDeviceProtectedStorageContext(): Context {
        return delegate.createDeviceProtectedStorageContext()
    }

    override fun openFileInput(name: String?): FileInputStream {
        return delegate.openFileInput(name)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getCodeCacheDir(): File {
        return delegate.codeCacheDir
    }

    override fun bindService(service: Intent?, conn: ServiceConnection?, flags: Int): Boolean {
        return delegate.bindService(service, conn, flags)
    }

    override fun deleteDatabase(name: String?): Boolean {
        return delegate.deleteDatabase(name)
    }

    override fun getAssets(): AssetManager {
        return delegate.assets
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getNoBackupFilesDir(): File {
        return delegate.noBackupFilesDir
    }

    override fun startActivities(intents: Array<out Intent>?) {
        delegate.startActivities(intents)
    }

    override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
        delegate.startActivities(intents, options)
    }

    override fun getResources(): Resources {
        return delegate.resources
    }

    override fun fileList(): Array<String> {
        return delegate.fileList()
    }

    override fun setTheme(resid: Int) {
        delegate.setTheme(resid)
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver?) {
        delegate.unregisterReceiver(receiver)
    }

    override fun enforcePermission(permission: String?, pid: Int, uid: Int, message: String?) {
        delegate.enforcePermission(permission, pid, uid, message)
    }

    override fun openFileOutput(name: String?, mode: Int): FileOutputStream {
        return delegate.openFileOutput(name, mode)
    }

    override fun sendStickyOrderedBroadcast(intent: Intent?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int,
                                            initialData: String?, initialExtras: Bundle?) {
        delegate.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras)
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration?): Context {
        return delegate.createConfigurationContext(overrideConfiguration)
    }

    override fun getFilesDir(): File {
        return delegate.filesDir
    }

    override fun sendBroadcast(intent: Intent?) {
        delegate.sendBroadcast(intent)
    }

    override fun sendBroadcast(intent: Intent?, receiverPermission: String?) {
        delegate.sendBroadcast(intent, receiverPermission)
    }

    override fun sendOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?,
                                            resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?,
                                            initialExtras: Bundle?) {
        delegate.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras)
    }

    override fun grantUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        delegate.grantUriPermission(toPackage, uri, modeFlags)
    }

    override fun enforceCallingUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        delegate.enforceCallingUriPermission(uri, modeFlags, message)
    }

    override fun getCacheDir(): File {
        return delegate.cacheDir
    }

    override fun clearWallpaper() {
        delegate.clearWallpaper()
    }

    override fun sendStickyOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, resultReceiver: BroadcastReceiver?,
                                                  scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        delegate.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras)
    }

    override fun startActivity(intent: Intent?) {
        delegate.startActivity(intent)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        delegate.startActivity(intent, options)
    }

    override fun getPackageManager(): PackageManager {
        return delegate.packageManager
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
        return delegate.openOrCreateDatabase(name, mode, factory)
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?,
                                      errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
        return delegate.openOrCreateDatabase(name, mode, factory, errorHandler)
    }

    override fun deleteFile(name: String?): Boolean {
        return delegate.deleteFile(name)
    }

    override fun startService(service: Intent?): ComponentName {
        return delegate.startService(service)
    }

    override fun revokeUriPermission(uri: Uri?, modeFlags: Int) {
        delegate.revokeUriPermission(uri, modeFlags)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun moveDatabaseFrom(sourceContext: Context?, name: String?): Boolean {
        return delegate.moveDatabaseFrom(sourceContext, name)
    }

    override fun startInstrumentation(className: ComponentName?, profileFile: String?, arguments: Bundle?): Boolean {
        return delegate.startInstrumentation(className, profileFile, arguments)
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?) {
        delegate.sendOrderedBroadcast(intent, receiverPermission)
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?, resultReceiver: BroadcastReceiver?, scheduler: Handler?,
                                      initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        delegate.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras)
    }

    override fun unbindService(conn: ServiceConnection?) {
        delegate.unbindService(conn)
    }

    override fun getApplicationInfo(): ApplicationInfo {
        return delegate.applicationInfo
    }

    override fun getWallpaperDesiredMinimumHeight(): Int {
        return delegate.wallpaperDesiredMinimumHeight
    }

    override fun createDisplayContext(display: Display?): Context {
        return delegate.createDisplayContext(display)
    }

    override fun getTheme(): Resources.Theme {
        return delegate.theme
    }

    override fun getPackageName(): String {
        return delegate.packageName
    }

    override fun getContentResolver(): ContentResolver {
        return delegate.contentResolver
    }

    override fun getObbDirs(): Array<File> {
        return delegate.obbDirs
    }

    override fun enforceCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        delegate.enforceCallingOrSelfUriPermission(uri, modeFlags, message)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
        return delegate.moveSharedPreferencesFrom(sourceContext, name)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getExternalMediaDirs(): Array<File> {
        return delegate.externalMediaDirs
    }

    override fun checkCallingPermission(permission: String?): Int {
        return delegate.checkCallingPermission(permission)
    }

    override fun getExternalCacheDirs(): Array<File> {
        return delegate.externalCacheDirs
    }

    override fun sendStickyBroadcast(intent: Intent?) {
        delegate.sendStickyBroadcast(intent)
    }

    override fun enforceCallingPermission(permission: String?, message: String?) {
        delegate.enforceCallingPermission(permission, message)
    }

    override fun peekWallpaper(): Drawable {
        return delegate.peekWallpaper()
    }

    override fun getSystemService(name: String?): Any {
        return delegate.getSystemService(name)
    }

    override fun getDir(name: String?, mode: Int): File {
        return delegate.getDir(name, mode)
    }

    override fun databaseList(): Array<String> {
        return delegate.databaseList()
    }

    override fun createPackageContext(packageName: String?, flags: Int): Context {
        return delegate.createPackageContext(packageName, flags)
    }

    override fun enforceUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) {
        delegate.enforceUriPermission(uri, pid, uid, modeFlags, message)
    }

    override fun enforceUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int,
                                      message: String?) {
        delegate.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message)
    }

    override fun removeStickyBroadcast(intent: Intent?) {
        delegate.removeStickyBroadcast(intent)
    }
}