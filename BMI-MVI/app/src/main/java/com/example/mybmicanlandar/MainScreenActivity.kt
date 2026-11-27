package com.example.mybmicanlandar

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

/** Android view adapter only. Subclasses connect an architecture-specific presentation layer. */
abstract class MainScreenActivity : Activity() {
    private lateinit var height: EditText
    private lateinit var weight: EditText
    private lateinit var age: EditText
    private lateinit var gender: RadioGroup
    private var rendering = false

    // 中文 · 子类在控件绑定完成后连接 Presenter、ViewModel 或 Store；三种架构共用下方的控件处理。
    // EN · Subclasses connect a Presenter, ViewModel or Store after widgets are bound. The widget handling below is shared by all three designs.
    protected abstract fun connect(restored: BmiForm?)
    protected abstract fun formChanged(form: BmiForm)
    protected abstract fun calculate()
    protected abstract fun menuAction(action: MenuAction)
    protected abstract fun disconnect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 中文 · 系统按方向选择同名布局。两个 XML 必须都提供下面查找的控件 ID。
        // EN · Android picks the layout for the current orientation. Both layouts must provide every widget ID looked up below.
        setContentView(R.layout.activity_main)
        setActionBar(findViewById<Toolbar>(R.id.toolbar))
        registerForContextMenu(findViewById<View>(R.id.bmiLogo))
        height = findViewById(R.id.heightInput)
        weight = findViewById(R.id.weightInput)
        age = findViewById(R.id.ageInput)
        gender = findViewById(R.id.genderGroup)
        // 中文 · 三个输入框共用监听器；rendering 为 true 时跳过回调，避免填回文字又被当成用户编辑。
        // EN · The three text fields share this listener. The rendering flag prevents programmatic updates from feeding back as user edits.
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) { if (!rendering) formChanged(readForm()) }
        }
        height.addTextChangedListener(watcher)
        weight.addTextChangedListener(watcher)
        age.addTextChangedListener(watcher)
        gender.setOnCheckedChangeListener { _, _ -> if (!rendering) formChanged(readForm()) }
        findViewById<Button>(R.id.calculateButton).setOnClickListener { calculate() }
        findViewById<Button>(R.id.aboutButton).setOnClickListener { menuAction(MenuAction.ABOUT) }
        age.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) { calculate(); true } else false
        }
        connect(savedInstanceState?.getSerializable("form") as? BmiForm)
    }

    // 中文 · 读取整个表单作为一次编辑快照，按钮 ID 在这里转换成与语言无关的性别枚举。
    // EN · Read one snapshot of the form. Radio button IDs are converted here into a language-independent enum.
    private fun readForm() = BmiForm(height.text.toString(), weight.text.toString(), age.text.toString(),
        when (gender.checkedRadioButtonId) {
            R.id.genderMale -> BmiCalculator.Gender.MALE
            R.id.genderFemale -> BmiCalculator.Gender.FEMALE
            else -> null
        })

    // 中文 · 只替换不同的文字，避免每次输入都重置光标；然后按保存的枚举恢复单选按钮。
    // EN · Only replace changed text to avoid resetting the cursor on each edit, then restore the radio selection from the enum.
    protected fun renderForm(form: BmiForm) {
        rendering = true
        if (height.text.toString() != form.height) height.setText(form.height)
        if (weight.text.toString() != form.weight) weight.setText(form.weight)
        if (age.text.toString() != form.age) age.setText(form.age)
        val id = when (form.gender) {
            BmiCalculator.Gender.MALE -> R.id.genderMale
            BmiCalculator.Gender.FEMALE -> R.id.genderFemale
            null -> -1
        }
        if (gender.checkedRadioButtonId != id) gender.check(id)
        rendering = false
    }

    protected fun showEffect(effect: UiEffect) {
        when (effect) {
            UiEffect.About -> AlertDialog.Builder(this).setTitle(R.string.about_button)
                .setMessage(R.string.about_message).setPositiveButton(android.R.string.ok, null).show()
            UiEffect.Wiki -> try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bmi_wiki_url))))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, R.string.browser_unavailable, Toast.LENGTH_LONG).show()
            }
            UiEffect.Close -> finish()
            // 中文 · 这里是两页之间的数据接口。修改 extra 名称或资料类型时，接收方 ReportScreenActivity 也要一起改。
            // EN · This is the boundary between screens. A change to the extra key or profile type must also reach ReportScreenActivity.
            is UiEffect.OpenReport -> startActivity(Intent(this, ReportActivity::class.java)
                .putExtra("profile", effect.profile))
            is UiEffect.Message -> Toast.makeText(this, when (effect.error) {
                InputError.MISSING_MEASUREMENTS -> R.string.input_required
                InputError.INVALID_NUMBER -> R.string.input_invalid
                InputError.NON_POSITIVE -> R.string.input_positive
                InputError.INVALID_AGE -> R.string.age_invalid
                InputError.MISSING_GENDER -> R.string.gender_required
            }, Toast.LENGTH_LONG).show()
        }
    }

    // 中文 · 临时草稿写入 Bundle，供系统重建界面时恢复；偏好文件另存最近一次有效计算的资料。
    // EN · The Bundle carries the draft across system recreation. Preferences separately hold the last successfully calculated profile.
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("form", readForm())
        super.onSaveInstanceState(outState)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }
    // 中文 · 长按已注册的 BMI 图标时加载上下文菜单；顶部工具栏使用独立的选项菜单资源。
    // EN · Long-pressing the registered BMI icon inflates the context menu. The toolbar uses its own options menu resource.
    override fun onCreateContextMenu(menu: ContextMenu, view: View, info: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, view, info)
        if (view.id == R.id.bmiLogo) menuInflater.inflate(R.menu.menu_context, menu)
    }
    // 中文 · 工具栏和长按菜单共享这个分派方法；相同 ID 的菜单项都会产生相同的 MenuAction。
    // EN · Both menu callbacks use this dispatcher, so a shared item ID produces the same MenuAction from either menu.
    private fun selected(item: MenuItem): Boolean {
        val action = when (item.itemId) {
            R.id.menu_about -> MenuAction.ABOUT
            R.id.menu_wiki -> MenuAction.WIKI
            R.id.menu_exit -> MenuAction.EXIT
            else -> return false
        }
        menuAction(action)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = selected(item) || super.onOptionsItemSelected(item)
    override fun onContextItemSelected(item: MenuItem) = selected(item) || super.onContextItemSelected(item)
    override fun onDestroy() { disconnect(); super.onDestroy() }
}
