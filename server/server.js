const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

const app = express();
const port = 3000;


    // Connect to MongoDB
    mongoose.connect('mongodb://127.0.0.1:27017/todo', {
        useNewUrlParser: true,
        useUnifiedTopology: true
    }).catch(error => console.error(error));


// Define user schema
const userSchema = new mongoose.Schema({
    username: {
        type: String,
        unique: true,
        required: true
    },
    password: {
        type: String,
        required: true
    },
    email: {
        type: String,
        required: true
    }
});
const User = mongoose.model('User', userSchema);

// Define list schema
const listSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true
    },
    creator: {
        type: String,
        required: true
    },
    shared: {
        type: Boolean,
        required: true
    }
});
const List = mongoose.model('List', listSchema);

// Define task schema
const taskSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true
    },
    list: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'List',
        required: true
    },
    done: {
        type: Boolean,
        required: true
    },
    taskId: {
        type: String,
        unique: true,
        required: true
    }
});
const Task = mongoose.model('Task', taskSchema);

// Parse incoming requests with JSON payloads
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

/**
 * @api {post} /users Create User
 * @apiName CreateUser
 * @apiGroup User
 *
 * @apiParam {String} username User's unique username.
 * @apiParam {String} password User's password.
 * @apiParam {String} email User's email.
 *
 * @apiSuccess {String} message User created successfully.
 * @apiError {String} message Error message.
 */
app.post('/users', async (req, res) => {
    const { username, password, email } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({ username });
    if (existingUser) {
        return res.status(409).json({ message: 'User already exists' });
    }

    // Create new user
    const user = new User({ username, password, email });
    await user.save();

    res.json({ message: 'User created successfully' });
});

/**
 * @api {post} /login Login
 * @apiName Login
 * @apiGroup User
 *
 * @apiParam {String} username User's username.
 * @apiParam {String} password User's password.
 *
 * @apiSuccess {String} message Login successful.
 * @apiError {String} message Error message.
 */
app.post('/login', async (req, res) => {
    const { username, password } = req.body;

    // Check if user exists and password matches
    const user = await User.findOne({ username, password });
    if (!user) {
        return res.status(401).json({ message: 'Invalid username or password' });
    }

    res.json({
        message: 'Login successful'
    });
});

/**

@api {post} /lists Create List
@apiName CreateList
@apiGroup List
@apiParam {String} name List's name.
@apiParam {String} creator User's username who created the list.
@apiParam {Boolean} shared Flag indicating if the list is shared.
@apiSuccess {String} message List created successfully.
@apiError {String} message Error message.
*/
app.post('/lists', async (req, res) => {
    const { name, creator, shared } = req.body;
	
    // Create new list
    const list = new List({ name, creator, shared });
    await list.save();

    res.json({ message: 'List created successfully' });
});

/**

@api {get} /lists Get Lists
@apiName GetLists
@apiGroup List
@apiSuccess {Object[]} lists List of all lists.
@apiSuccess {String} lists._id List's unique identifier.
@apiSuccess {String} lists.name List's name.
@apiSuccess {String} lists.creator User's username who created the list.
@apiSuccess {Boolean} lists.shared Flag indicating if the list is shared.
*/
app.get('/lists', async (req, res) => {
    const lists = await List.find();
    res.json(lists);
});
/**

@api {delete} /lists/:username/:name Delete List
@apiName DeleteList
@apiGroup List
@apiParam {String} username User's username.
@apiParam {String} name List's name.
@apiSuccess {String} message List deleted successfully.
@apiError {String} message Error message.
*/
app.delete('/lists/:username/:name', async (req, res) => {
    const { username, name } = req.params;
  
    // Find list with given name and creator
    const list = await List.findOne({ name, creator: username });
    if (!list) {
      return res.status(404).json({ message: 'List not found' });
    }
  
    // Delete list and associated tasks
    try {
      await Task.deleteMany({ list: list._id });
      await List.deleteOne({ _id: list._id });
  
      res.json({ message: 'List deleted successfully' });
    } catch (error) {
      console.error(error);
      res.status(500).json({ message: 'Internal Server Error' });
    }
  });
  

/**

@api {post} /tasks Create Task
@apiName CreateTask
@apiGroup Task
@apiParam {String} name Task's name.
@apiParam {String} list List's name.
@apiParam {Boolean} done Flag indicating if the task is done.
@apiParam {String} taskId Task's unique identifier.
@apiSuccess {String} message Task created successfully.
@apiError {String} message Error message.
*/
app.post('/tasks', async (req, res) => {
    const { name, list, done, taskId } = req.body;
    // Check if list exists
    const existingList = await List.findOne({ name: list });
    if (!existingList) {
        return res.status(404).json({ message: 'List not found' });
    }

    // Create new task
    const task = new Task({ name, list: existingList._id, done, taskId });
    await task.save();

    res.json({ message: 'Task created successfully' });
});

/**

@api {delete} /tasks/:id Delete Task
@apiName DeleteTask
@apiGroup Task
@apiParam {String} id Task's unique identifier.
@apiSuccess {String} message Task deleted successfully.
@apiError {String} message Error message.
*/

app.delete('/tasks/:id', async (req, res) => {
    const { id } = req.params;
  
    // Validate ID format
    /*if (!mongoose.Types.ObjectId.isValid(id)) {
      return res.status(400).json({ message: 'Invalid task ID' });
    }*/
  
    // Delete task
    try {
      const result = await Task.deleteOne({ taskId : id });
  
      if (result.deletedCount === 0) {
        return res.status(404).json({ message: 'Task not found' });
      }
  
      res.json({ message: 'Task deleted successfully' });
    } catch (error) {
      console.error(error);
      res.status(500).json({ message: 'Internal Server Error' });
    }
  });

/**
 
@api {get} /tasks/:list Get Tasks by List
@apiName GetTasksByList
@apiGroup Task
@apiParam {String} list List's name.
@apiSuccess {Object[]} tasks List of all tasks for the given list.
@apiSuccess {String} tasks._id Task's unique identifier.
@apiSuccess {String} tasks.name Task's name.
@apiSuccess {String} tasks.list List's unique identifier.
@apiSuccess {Boolean} tasks.done Flag indicating if the task is done.
@apiSuccess {String} tasks.taskId Task's unique identifier.
*/
app.get('/tasks/:list', async (req, res) => {
    const { list } = req.params;
    // Find list with given name
    const existingList = await List.findOne({ name: list });
    if (!existingList) {
        return res.status(404).json({ message: 'Task not found' });
    }

    // Find all tasks for the given list
    const tasks = await Task.find({ list: existingList._id });
    res.json(tasks);
});

app.listen(port, () => {
    console.log(`Server started on port ${port}`);
});


/**

@api {post} /tasks Change Task done
@apiName ChangeDone
@apiGroup Task
@apiParam {String} name Task's name.
@apiParam {String} list List's name.
@apiParam {Boolean} done Flag indicating if the task is done.
@apiParam {String} taskId Task's unique identifier.
@apiSuccess {String} message Task created successfully.
@apiError {String} message Error message.
*/
app.post('/tasks/change', async (req, res) => {
    const { task_id, is_done } = req.body;
	
    // Check if list exists
    const existingTask = await Task.findOne({ taskId: task_id });
    if (!existingTask) {
        return res.status(404).json({ message: 'Task not found' });
    }

    // Create new task
    existingTask.done = is_done;
	await existingTask.save();

    res.json({ message: 'Task changed successfully' });
});